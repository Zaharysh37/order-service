package com.innowise.orderservice.core.dao;

import com.innowise.orderservice.core.entity.Order;
import com.innowise.orderservice.core.entity.Status;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByIdIn(Collection<Long> ids);

    List<Order> findAllByStatusIn(Collection<Status> statuses);

    List<Order> findAllByUserId(Long userId);
}
