package com.supersys.lambda.config;

import com.supersys.lambda.client.AiServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ClientConfig {

    @Bean
    public AiServiceClient aiServiceClient(RestClient.Builder restClientBuilder) {
        RestClient restClient = restClientBuilder.baseUrl("http://ai-service").build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(AiServiceClient.class);
    }
}
