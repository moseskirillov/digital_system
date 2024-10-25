package org.wolrus.digital_system.exception;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.wolrus.digital_system.feign.TelegramReportClient;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ControllerAdvice {

    @Value("${telegram.service-admin-id}")
    private String TELEGRAM_ADMIN_ID;

    private static final String FEIGN_ERROR_MESSAGE = "Ошибка при отправке запроса: %s";
    private static final String CONTROLLER_ERROR_MESSAGE = "Ошибка при обработке запроса: %s";

    private final TelegramReportClient telegramReportClient;

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Void> exception(FeignException e) {
        log.error(FEIGN_ERROR_MESSAGE, e);
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> exception(Exception e) {
        log.error(CONTROLLER_ERROR_MESSAGE, e);
        telegramReportClient.sendMessage(TELEGRAM_ADMIN_ID, String.format(CONTROLLER_ERROR_MESSAGE, e.getMessage()), true, null);
        return ResponseEntity.badRequest().build();
    }

}
