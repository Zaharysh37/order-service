package com.innowise.orderservice.core.mapper.itemmapper;

import com.innowise.orderservice.api.dto.item.GetItemDto;
import com.innowise.orderservice.core.entity.Item;
import com.innowise.orderservice.core.mapper.BaseMapper;
import org.mapstruct.Mapper;

@Mapper(config = BaseMapper.class)
public interface GetItemMapper extends BaseMapper<Item, GetItemDto> {
}
