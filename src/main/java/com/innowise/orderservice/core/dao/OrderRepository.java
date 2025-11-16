package com.innowise.orderservice.core.dao;

import com.innowise.orderservice.core.entity.Order;
import com.innowise.orderservice.core.entity.Status;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByIdInOrderByCreationDateDesc(Collection<Long> ids);

    @Query("SELECT o FROM Order o WHERE o.status IN :statuses ORDER BY o.creationDate DESC")
    List<Order> findAllByStatusInOrderByCreatedAtDesc(@Param("statuses")Collection<Status> statuses);

    @Query(value = "SELECT o FROM orders o WHERE o.user_id = :userId ORDER BY o.creation_date DESC", nativeQuery = true)
    List<Order> findAllByUserIdOrderByCreatedDesc(@Param("userId")Long userId);
}
