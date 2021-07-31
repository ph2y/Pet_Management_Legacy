package com.sju18.petmanagement.domain.pet.dto;

import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

@Data
public class PetFetchReqDto {
    @PositiveOrZero(message = "valid.pet.id.notNegative")
    private Long id;
}
