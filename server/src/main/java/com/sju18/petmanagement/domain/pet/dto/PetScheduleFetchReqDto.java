package com.sju18.petmanagement.domain.pet.dto;

import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

@Data
public class PetScheduleFetchReqDto {
    @PositiveOrZero(message = "valid.petSchedule.id.notNegative")
    private Long id;
}
