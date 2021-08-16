package com.sju18.petmanagement.domain.map.place.dto;

import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

@Data
public class DeletePlaceReqDto {
    @PositiveOrZero(message = "valid.place.id.notNegative")
    Long id;
}
