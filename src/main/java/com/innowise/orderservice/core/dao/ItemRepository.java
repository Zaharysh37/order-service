package com.innowise.orderservice.core.dao;

import com.innowise.orderservice.core.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

}
