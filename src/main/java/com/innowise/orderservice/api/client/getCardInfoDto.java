package com.innowise.orderservice.api.client;

public record getCardInfoDto(
    Long id,
    String number,
    String expirationDate
) {}
