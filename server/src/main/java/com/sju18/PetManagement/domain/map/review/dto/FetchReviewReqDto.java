package com.sju18.petmanagement.domain.map.review.dto;

import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

@Data
public class FetchReviewReqDto {
    @PositiveOrZero(message = "valid.review.id.notNegative")
    private Long id;
    @PositiveOrZero(message = "valid.review.placeId.notNegative")
    private Long placeId;
    @PositiveOrZero(message = "valid.review.authorId.notNegative")
    private Long authorId;
}
