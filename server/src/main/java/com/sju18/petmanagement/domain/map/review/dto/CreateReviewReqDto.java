package com.sju18.petmanagement.domain.map.review.dto;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class CreateReviewReqDto {
    @PositiveOrZero(message = "valid.review.placeId.notNegative")
    private Long placeId;
    @NotNull(message = "valid.review.rating.null")
    @Max(value = 5, message = "valid.review.rating.max")
    @Min(value = 1, message = "valid.review.rating.min")
    private Integer rating;
    @NotBlank(message = "valid.review.contents.blank")
    @Size(max = 1000, message = "valid.review.contents.size")
    private String contents;
}
