package com.innowise.orderservice.api.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.LocalDate;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserClient {

    Logger logger = LoggerFactory.getLogger(UserClient.class);

    @GetMapping("api/users/email")
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetUserByEmail")
    GetUserDto getUserByEmail(@RequestParam("email") String email);

    @GetMapping("api/users/{id}")
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetUserById")
    GetUserDto getUserById(@PathVariable("id") Long id);

    default GetUserDto fallbackGetUserByEmail(String email, Throwable throwable) {
        logger.warn("UserService (by email) is unavailable. Fallback executed.", throwable.getMessage());
        return new GetUserDto(-1L, "User", "Unavailable", LocalDate.MAX, email, Collections.emptyList());
    }

    default GetUserDto fallbackGetUserById(Long id, Throwable throwable) {
        logger.warn("UserService (by id) is unavailable. Fallback executed.", throwable.getMessage());
        return new GetUserDto(id, "User", "Unavailable", LocalDate.MAX, "N/A", Collections.emptyList());
    }
}
