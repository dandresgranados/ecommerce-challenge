package com.tgs.ecommerce.order.service;

import com.tgs.ecommerce.order.domain.DiscountWindow;
import com.tgs.ecommerce.order.dto.DiscountWindowResponse;

public final class DiscountWindowMapper {

    private DiscountWindowMapper() {}

    public static DiscountWindowResponse toResponse(DiscountWindow w) {
        return new DiscountWindowResponse(
            w.getId(),
            w.getName(),
            w.getType(),
            w.getRate(),
            w.getStartAt(),
            w.getEndAt(),
            w.getActive(),
            w.getCreatedAt(),
            w.getUpdatedAt()
        );
    }
}
