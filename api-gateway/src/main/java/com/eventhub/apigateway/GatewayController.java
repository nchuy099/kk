package com.eventhub.apigateway;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayController {
    private final GatewayProxyService proxyService;

    public GatewayController(GatewayProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @RequestMapping(path = "/api/**")
    public void proxy(HttpServletRequest request, HttpServletResponse response) throws IOException, InterruptedException {
        proxyService.proxy(request, response);
    }
}
