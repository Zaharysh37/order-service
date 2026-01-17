package com.innowise.orderservice.api.controller;

import com.innowise.orderservice.api.dto.order.CreateOrderDto;
import com.innowise.orderservice.api.dto.order.GetOrderDto;
import com.innowise.orderservice.core.entity.Status;
import com.innowise.orderservice.core.service.OrderService;
import jakarta.validation.Valid;
import java.nio.file.AccessDeniedException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping
    public ResponseEntity<GetOrderDto> createOrder(@Valid @RequestBody CreateOrderDto order) {
        GetOrderDto createdOrder = orderService.createOrder(order);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<GetOrderDto>> getAllOrders(Pageable pageable) {
        Page<GetOrderDto> orders = orderService.getAllOrders(pageable);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetOrderDto> getOrderById(@PathVariable Long id)
        throws AccessDeniedException {
        GetOrderDto order = orderService.getOrderById(id);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @GetMapping("/by-ids")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<GetOrderDto>> getOrdersByIds(@RequestParam("ids") List<Long> ids,
                                                            Pageable pageable) {
        Page<GetOrderDto> orders = orderService.getOrdersByIds(ids, pageable);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/by-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<GetOrderDto>> getOrdersByStatuses(@RequestParam("statuses") List<Status> statuses,
                                                              Pageable pageable) {
        Page<GetOrderDto> orders = orderService.getOrdersByStatuses(statuses, pageable);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GetOrderDto> updateOrder(@PathVariable Long id,
                                                   @RequestParam("status") Status status) {
        GetOrderDto order = orderService.updateOrderStatus(id, status);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) throws AccessDeniedException {
        orderService.deleteOrder(id);
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }
}
