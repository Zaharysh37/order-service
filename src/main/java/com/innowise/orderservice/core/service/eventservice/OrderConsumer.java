package com.innowise.orderservice.core.service.eventservice;

import com.innowise.orderservice.api.dto.eventdto.PaymentEventDto;
import com.innowise.orderservice.api.dto.eventdto.PaymentStatus;
import com.innowise.orderservice.core.dao.OrderRepository;
import com.innowise.orderservice.core.entity.Order;
import com.innowise.orderservice.core.entity.Status;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final OrderRepository orderRepository;

    @Transactional
    @KafkaListener(topics = "${kafka.topics.consumer}",
    groupId = "${spring.kafka.consumer.group-id}")
    public void handleCreateOrderEvent(PaymentEventDto eventDto) {

        log.info("Received CreatePaymentEvent from Kafka: {}", eventDto);

        if (eventDto.status() == PaymentStatus.SUCCESS) {

            Long orderId = eventDto.orderId();

            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order for payment not found: " + orderId));

            order.setStatus(Status.PROCESSED);

            orderRepository.save(order);
        }
    }
}
