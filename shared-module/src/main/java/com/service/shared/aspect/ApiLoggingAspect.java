package com.service.shared.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.shared.annotation.MethodCode;
import com.service.shared.document.ApiLog;
import com.service.shared.repository.ApiLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Aspect to log all API requests and responses to MongoDB
 * Works with any controller package by using a configurable base package
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ApiLoggingAspect {

    private final ApiLogRepository apiLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Intercepts all controller methods or methods annotated with @MethodCode
     * The pointcut will match any controller in any service package
     */
    @Around("@annotation(com.service.shared.annotation.MethodCode) || " +
            "execution(* *..controller..*(..))")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        LocalDateTime requestTimestamp = LocalDateTime.now();
        
        ApiLog.ApiLogBuilder logBuilder = ApiLog.builder()
                .requestTimestamp(requestTimestamp);

        try {
            // Get method information
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String methodName = signature.getMethod().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            
            logBuilder.methodName(methodName);

            // Get method code from annotation
            MethodCode methodCodeAnnotation = signature.getMethod().getAnnotation(MethodCode.class);
            String methodCode = methodCodeAnnotation != null 
                ? methodCodeAnnotation.value() 
                : generateDefaultMethodCode(className, methodName);
            String methodDescription = methodCodeAnnotation != null 
                ? methodCodeAnnotation.description() 
                : null;
            
            logBuilder.methodCode(methodCode);
            if (methodDescription != null) {
                logBuilder.methodDescription(methodDescription);
            }

            // Get HTTP request information
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                logBuilder.httpMethod(request.getMethod())
                        .url(request.getRequestURI())
                        .clientIp(getClientIpAddress(request))
                        .userAgent(request.getHeader("User-Agent"))
                        .requestHeaders(serializeHeaders(request));
            }

            // Get request body
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                for (Object arg : args) {
                    if (arg instanceof ResponseEntity) {
                        // Skip ResponseEntity as it's a response, not part of the request body
                        continue;
                    }
                    try {
                        logBuilder.requestBody(objectMapper.writeValueAsString(arg));
                        break; // Assuming only one request body argument
                    } catch (Exception e) {
                        log.debug("Could not serialize request argument to JSON: {}", arg.getClass().getName());
                    }
                }
            }

            // Execute the method
            Object result = joinPoint.proceed();
            
            long executionTime = System.currentTimeMillis() - startTime;
            LocalDateTime responseTimestamp = LocalDateTime.now();

            logBuilder.executionTime(executionTime)
                    .responseTimestamp(responseTimestamp);

            // Get response information
            if (result instanceof ResponseEntity<?> responseEntity) {
                logBuilder.statusCode(responseEntity.getStatusCode().value())
                        .responseHeaders(responseEntity.getHeaders().toString());
                
                try {
                    Object responseBody = responseEntity.getBody();
                    if (responseBody != null) {
                        logBuilder.responseBody(objectMapper.writeValueAsString(responseBody));
                    }
                } catch (Exception e) {
                    log.debug("Could not serialize response body: {}", e.getMessage());
                }
            } else if (result != null) {
                logBuilder.statusCode(org.springframework.http.HttpStatus.OK.value());
                try {
                    logBuilder.responseBody(objectMapper.writeValueAsString(result));
                } catch (Exception e) {
                    log.debug("Could not serialize response: {}", e.getMessage());
                }
            }

            // Save to MongoDB (with error handling)
            try {
                ApiLog apiLog = logBuilder.build();
                apiLogRepository.save(apiLog);
                log.debug("API call logged: methodCode={}, method={}, executionTime={}ms", 
                    methodCode, methodName, executionTime);
            } catch (Exception mongoException) {
                // Log MongoDB errors but don't fail the request
                log.warn("Failed to save API log to MongoDB: {}", mongoException.getMessage());
            }

            return result;

        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - startTime;
            LocalDateTime responseTimestamp = LocalDateTime.now();

            logBuilder.executionTime(executionTime)
                    .responseTimestamp(responseTimestamp)
                    .statusCode(500)
                    .errorMessage(throwable.getMessage())
                    .stackTrace(getStackTrace(throwable));

            // Save error log to MongoDB (with error handling)
            try {
                ApiLog apiLog = logBuilder.build();
                apiLogRepository.save(apiLog);
                log.error("API call failed and logged: methodCode={}, method={}", 
                    logBuilder.build().getMethodCode(), 
                    logBuilder.build().getMethodName(), 
                    throwable);
            } catch (Exception mongoException) {
                // Log MongoDB errors but don't fail the request
                log.warn("Failed to save error log to MongoDB: {}", mongoException.getMessage());
            }

            throw throwable;
        }
    }

    /**
     * Generate default method code if annotation is not present
     */
    private String generateDefaultMethodCode(String className, String methodName) {
        // Generate code based on class and method name
        // Format: First 2 letters of class + First 2 letters of method + 001
        String classPrefix = className.length() >= 2 
            ? className.substring(0, 2).toUpperCase() 
            : className.toUpperCase();
        String methodPrefix = methodName.length() >= 2 
            ? methodName.substring(0, 2).toUpperCase() 
            : methodName.toUpperCase();
        return classPrefix + methodPrefix + "001";
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * Serialize request headers to JSON string
     */
    private String serializeHeaders(HttpServletRequest request) {
        try {
            Map<String, String> headers = new HashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.put(headerName, request.getHeader(headerName));
            }
            return objectMapper.writeValueAsString(headers);
        } catch (Exception e) {
            log.debug("Could not serialize headers: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get stack trace as string
     */
    private String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
