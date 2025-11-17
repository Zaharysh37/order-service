package com.innowise.orderservice.api.controller;

import com.innowise.orderservice.api.dto.order.CreateOrderDto;
import com.innowise.orderservice.api.dto.order.GetOrderDto;
import com.innowise.orderservice.core.dao.OrderRepository;
import com.innowise.orderservice.core.entity.Status;
import com.innowise.orderservice.core.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<GetOrderDto> createOrder(@Valid @RequestBody
                                                       CreateOrderDto order) {
        GetOrderDto createdOrder = orderService.createOrder(order);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<GetOrderDto>> getAllOrders() {
        List<GetOrderDto> orders = orderService.getAllOrders();
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetOrderDto> getOrderById(@PathVariable Long id) {
        GetOrderDto order = orderService.getOrderById(id);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @GetMapping("/by-ids")
    public ResponseEntity<List<GetOrderDto>> getOrdersByIds(@RequestParam("ids") List<Long> ids) {
        List<GetOrderDto> orders = orderService.getOrdersByIds(ids);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/by-status")
    public ResponseEntity<List<GetOrderDto>> getOrdersByEmail(@RequestBody List<Status> statuses) {
        List<GetOrderDto> orders = orderService.getOrdersByStatuses(statuses);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GetOrderDto> updateOrder(@PathVariable Long id,
                                                   @RequestParam("status") Status status) {
        GetOrderDto order = orderService.getOrderById(id);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }
}
