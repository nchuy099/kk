package com.eventhub.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import com.eventhub.common.web.CommonRequestLoggingConfig;

@SpringBootApplication
@EnableConfigurationProperties(KeycloakAdminProperties.class)
@Import(CommonRequestLoggingConfig.class)
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
