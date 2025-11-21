package com.innowise.orderservice.core.dao;

import com.innowise.orderservice.core.entity.Order;
import com.innowise.orderservice.core.entity.Status;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findAllByIdIn(Collection<Long> ids, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status IN :statuses")
    Page<Order> findAllByStatusIn(@Param("statuses")Collection<Status> statuses, Pageable pageable);

    @Query(value = "SELECT o FROM orders o WHERE o.user_id = :userId", nativeQuery = true)
    Page<Order> findAllByUserId(@Param("userId")Long userId, Pageable pageable);
}
