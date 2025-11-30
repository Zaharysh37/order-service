package com.innowise.orderservice.api.client;

import com.innowise.orderservice.core.config.FeignClientConfig;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "userservice",
    url = "${user.service.url}",
    path = "/api/users",
    configuration = FeignClientConfig.class
)
public interface UserClient {

    @GetMapping("/email")
    @CircuitBreaker(name = "userService")
    GetUserDto getUserByEmail(@RequestParam("email") String email);

    @GetMapping("/{id}")
    @CircuitBreaker(name = "userService")
    GetUserDto getUserById(@PathVariable("id") Long id);

    @PostMapping("/batch/id")
    @CircuitBreaker(name = "userService")
    List<GetUserDto> getAllById(@RequestBody List<Long> ids);
}
