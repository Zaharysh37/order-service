package com.innowise.orderservice.api.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                if(requestAttributes != null) {
                    String token = requestAttributes.getRequest().getHeader("Authorization");

                    if(token != null && token.startsWith("Bearer ")) {
                        template.header("Authorization", token);
                    }
                }
            }
        };
    }
}
