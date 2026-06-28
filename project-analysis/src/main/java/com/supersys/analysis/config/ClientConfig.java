package com.supersys.analysis.config;

import com.supersys.analysis.client.AiServiceClient;
import com.supersys.analysis.client.AiLambdaServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ClientConfig {

    @Value("${services.ai-service.url:http://ai-service:8082}")
    private String aiServiceUrl;

    @Value("${services.lambda-service.url:http://lambda-service:8080}")
    private String lambdaServiceUrl;

    @Bean
    @LoadBalanced
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public AiServiceClient aiServiceClient(RestClient.Builder builder) {
        RestClient restClient = builder.baseUrl(aiServiceUrl).build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(AiServiceClient.class);
    }

    @Bean
    public AiLambdaServiceClient aiLambdaServiceClient(RestClient.Builder builder) {
        RestClient restClient = builder.baseUrl(lambdaServiceUrl).build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(AiLambdaServiceClient.class);
    }
}
