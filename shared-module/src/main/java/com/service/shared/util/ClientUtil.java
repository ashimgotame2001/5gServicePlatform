package com.service.shared.util;

import com.service.shared.exception.GlobalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@Slf4j
public class ClientUtil {
    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);



}
