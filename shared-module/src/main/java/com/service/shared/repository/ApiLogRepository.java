package com.service.shared.repository;

import com.service.shared.document.ApiLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB repository for API logs
 */
@Repository
public interface ApiLogRepository extends MongoRepository<ApiLog, String> {

    /**
     * Find logs by method code
     */
    List<ApiLog> findByMethodCodeOrderByRequestTimestampDesc(String methodCode);

    /**
     * Find logs by method name
     */
    List<ApiLog> findByMethodNameOrderByRequestTimestampDesc(String methodName);

    /**
     * Find logs by URL
     */
    List<ApiLog> findByUrlOrderByRequestTimestampDesc(String url);

    /**
     * Find logs within a time range
     */
    List<ApiLog> findByRequestTimestampBetweenOrderByRequestTimestampDesc(
            LocalDateTime start, LocalDateTime end);

    /**
     * Find logs by status code
     */
    List<ApiLog> findByStatusCodeOrderByRequestTimestampDesc(Integer statusCode);

    /**
     * Find logs with errors
     */
    List<ApiLog> findByErrorMessageIsNotNullOrderByRequestTimestampDesc();
}
