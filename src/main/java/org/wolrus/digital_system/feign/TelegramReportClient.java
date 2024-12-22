package org.wolrus.digital_system.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.wolrus.digital_system.config.FeignConfig;
import org.wolrus.digital_system.model.TelegramRequest;

@FeignClient(value = "telegram-report", configuration = FeignConfig.class)
public interface TelegramReportClient {
    @PostMapping("/sendMessage")
    void sendMessage(@RequestParam("chat_id") String chatId,
                     @RequestParam("text") String text,
                     @RequestParam("disable_web_page_preview") Boolean disableWebPagePreview,
                     @RequestParam("parse_mode") String parseMode);

    @PostMapping("/sendMessage")
    void sendMessage(@RequestBody TelegramRequest telegramRequest);

}
