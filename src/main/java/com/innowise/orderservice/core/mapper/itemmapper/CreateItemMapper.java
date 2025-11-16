package com.innowise.orderservice.core.mapper.itemmapper;

import com.innowise.orderservice.api.dto.item.CreateItemDto;
import com.innowise.orderservice.core.entity.Item;
import com.innowise.orderservice.core.mapper.BaseMapper;
import org.mapstruct.Mapper;

@Mapper(config = BaseMapper.class)
public interface CreateItemMapper extends BaseMapper<Item, CreateItemDto> {
}
