package com.supersys.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class McpServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(McpServiceApplication.class, args);
    }
}
