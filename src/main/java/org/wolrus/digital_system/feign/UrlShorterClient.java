package org.wolrus.digital_system.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.wolrus.digital_system.config.FeignConfig;

@FeignClient(name = "url-shorter", configuration = FeignConfig.class)
public interface UrlShorterClient {
    @GetMapping("--")
    String urlShorter(@RequestParam String url);
}
