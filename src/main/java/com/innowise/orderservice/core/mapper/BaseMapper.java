package com.innowise.orderservice.core.mapper;

import java.util.List;

@MapperConfig(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT
)
public interface BaseMapper<E, D> {

    D toDto(E entity);

    E toEntity(D dto);

    List<D> toDtos(Iterable<E> entities);

    List<E> toEntities(Iterable<D> dtos);

    E merge(@MappingTarget E entity, D dto);
}
