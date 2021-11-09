package com.sju18.petmanagement.domain.map.review.dto;

import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

@Data
public class FetchReviewReqDto {
    @PositiveOrZero(message = "valid.review.id.notNegative")
    private Long id;
    @PositiveOrZero(message = "valid.place.id.notNegative")
    private Long placeId;
    @PositiveOrZero(message = "valid.account.id.notNegative")
    private Long authorId;
}
