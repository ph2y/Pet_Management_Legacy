package com.sju18.petmanagement.domain.map.review.dto;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class UpdateReviewReqDto {
    @PositiveOrZero(message = "valid.review.id.notNegative")
    Long id;
    @Max(value = 5, message = "valid.review.rating.max")
    @Min(value = 1, message = "valid.review.rating.min")
    private Integer rating;
    @Size(max = 1000, message = "valid.review.contents.size")
    private String contents;
}
