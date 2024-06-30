package org.wolrus.digital_system.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableFeignClients(basePackages = {"org.wolrus.digital_system.feign"})
public class AppConfig {}
