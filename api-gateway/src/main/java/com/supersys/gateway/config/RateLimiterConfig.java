package com.supersys.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter.Response;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = "unknown";
            if (exchange.getRequest().getRemoteAddress() != null && exchange.getRequest().getRemoteAddress().getAddress() != null) {
                ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            }
            return Mono.just(ip);
        };
    }

    @Bean
    public RateLimiter<Object> customRateLimiter() {
        return new RateLimiter<Object>() {
            
            // Bucket de tokens em memória simples por IP
            private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
            private final int capacity = 10000; // Limite de 10.000 requisições
            private final int refillTokens = 10000;
            private final int refillPeriodSeconds = 60; // 10.000 tokens a cada 60 segundos (limite bem alto)

            @Override
            public Mono<Response> isAllowed(String routeId, String id) {
                TokenBucket bucket = buckets.computeIfAbsent(id, k -> new TokenBucket(capacity, refillTokens, refillPeriodSeconds));
                boolean allowed = bucket.tryConsume();
                Map<String, String> headers = Map.of(
                    "X-RateLimit-Remaining", String.valueOf(bucket.getRemainingTokens()),
                    "X-RateLimit-Limit", String.valueOf(capacity)
                );
                return Mono.just(new Response(allowed, headers));
            }

            @Override
            public Map<String, Object> getConfig() {
                return Collections.emptyMap();
            }

            @Override
            public Class<Object> getConfigClass() {
                return Object.class;
            }

            @Override
            public Object newConfig() {
                return new Object();
            }
        };
    }

    private static class TokenBucket {
        private final int capacity;
        private final int refillTokens;
        private final long refillPeriodSeconds;
        private double tokens;
        private long lastRefillTime;

        public TokenBucket(int capacity, int refillTokens, long refillPeriodSeconds) {
            this.capacity = capacity;
            this.refillTokens = refillTokens;
            this.refillPeriodSeconds = refillPeriodSeconds;
            this.tokens = capacity;
            this.lastRefillTime = Instant.now().getEpochSecond();
        }

        public synchronized boolean tryConsume() {
            refill();
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }

        public synchronized int getRemainingTokens() {
            refill();
            return (int) Math.floor(tokens);
        }

        private void refill() {
            long now = Instant.now().getEpochSecond();
            long elapsedTime = now - lastRefillTime;
            if (elapsedTime > 0) {
                double tokensToAdd = elapsedTime * ((double) refillTokens / refillPeriodSeconds);
                tokens = Math.min(capacity, tokens + tokensToAdd);
                lastRefillTime = now;
            }
        }
    }
}
