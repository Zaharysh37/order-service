package com.innowise.orderservice.core.service.integration;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.innowise.orderservice.api.dto.eventdto.OrderEventDto;
import com.innowise.orderservice.api.dto.eventdto.PaymentEventDto;
import com.innowise.orderservice.api.dto.eventdto.PaymentStatus;
import com.innowise.orderservice.api.dto.item.CreateItemDto;
import com.innowise.orderservice.api.dto.item.GetItemDto;
import com.innowise.orderservice.api.dto.order.CreateOrderDto;
import com.innowise.orderservice.api.dto.order.GetOrderDto;
import com.innowise.orderservice.api.dto.order.orderitem.CreateOrderItemDto;
import com.innowise.orderservice.core.dao.ItemRepository;
import com.innowise.orderservice.core.dao.OrderRepository;
import com.innowise.orderservice.core.entity.Item;
import com.innowise.orderservice.core.entity.Order;
import com.innowise.orderservice.core.entity.Status;
import com.innowise.orderservice.core.security.SecurityHelper;
import com.innowise.orderservice.core.service.ItemService;
import com.innowise.orderservice.core.service.OrderService;
import feign.FeignException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.transaction.support.TransactionTemplate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrderServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired private OrderService orderService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private KafkaTemplate<String, Object> kafkaTemplate;
    private Consumer<String, OrderEventDto> testConsumer;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @MockBean
    private SecurityHelper securityHelper;

    @BeforeEach
    void setUp() {

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
            kafkaContainer.getBootstrapServers(),
            "test-group",
            "true");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<OrderEventDto> deserializer = new JsonDeserializer<>(OrderEventDto.class);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(false);

        DefaultKafkaConsumerFactory<String, OrderEventDto> cf = new DefaultKafkaConsumerFactory<>(
            consumerProps,
            new StringDeserializer(),
            deserializer
        );

        testConsumer = cf.createConsumer();
        testConsumer.subscribe(Collections.singletonList("create-order"));
    }

    @AfterEach
    void cleanup() {

        orderRepository.deleteAll();
        itemRepository.deleteAll();

        if (testConsumer != null) {
            testConsumer.close();
        }
    }

    private void stubUserServiceByEmail(String email, Long userId) {
        stubFor(WireMock.get(urlPathEqualTo("/api/users/email"))
            .withQueryParam("email", equalTo(email))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("""
                    {
                        "id": %d,
                        "name": "Test",
                        "surname": "User",
                        "email": "%s",
                        "cards": []
                    }
                    """.formatted(userId, email))));
    }

    private void stubUserServiceById(Long userId) {
        stubFor(WireMock.get(urlPathEqualTo("/api/users/" + userId))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("""
                    {
                        "id": %d,
                        "name": "Test",
                        "surname": "User",
                        "email": "test@mail.com",
                        "cards": []
                    }
                    """.formatted(userId))));
    }

    @Test
    @WithMockUser(roles = "USER")
    void test_createOrder_Success() {

        Item item = new Item();
        item.setName("Phone");
        item.setPrice(BigDecimal.TEN);
        item = itemRepository.save(item);

        String userEmail = "ivan@mail.com";
        Long userId = 101L;
        CreateOrderDto orderDto = new CreateOrderDto(
            userEmail,
            List.of(new CreateOrderItemDto(item.getId(), 2))
        );

        stubUserServiceByEmail(userEmail, userId);

        GetOrderDto result = orderService.createOrder(orderDto);

        ConsumerRecord<String, OrderEventDto> record = KafkaTestUtils.
            getSingleRecord(testConsumer, "create-order", Duration.ofSeconds(10));

        OrderEventDto orderEventDto = record.value();
        assertThat(orderEventDto).isNotNull();
        assertThat(orderEventDto.orderId()).isEqualTo(result.getOrderDtoWithoutUser().id());
        assertThat(orderEventDto.userId()).isEqualTo(userId);
        assertThat(orderEventDto.amount()).isEqualByComparingTo(BigDecimal.valueOf(20));

        PaymentEventDto paymentEvent = new PaymentEventDto("694a6081723088150e7cf74c",
            orderEventDto.orderId(), orderEventDto.userId(), PaymentStatus.SUCCESS);

        kafkaTemplate.send("create-payment", paymentEvent);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            transactionTemplate.execute(status -> {
                List<Order> orders = orderRepository.findAll();
                assertThat(orders).hasSize(1);
                Order order = orders.get(0);

                assertThat(order.getUserId()).isEqualTo(userId);
                assertThat(order.getStatus()).isEqualTo(Status.PROCESSED);

                assertThat(order.getOrderItems()).hasSize(1);
                assertThat(order.getOrderItems().iterator().next().getQuantity()).isEqualTo(2);

                return null;
            });
        });
    }
}