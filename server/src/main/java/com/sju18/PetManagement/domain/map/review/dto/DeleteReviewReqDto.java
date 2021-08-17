package com.sju18.petmanagement.domain.map.review.dto;

import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

@Data
public class DeleteReviewReqDto {
    @PositiveOrZero(message = "valid.review.id.notNegative")
    Long id;
}
