package com.sju18.petmanagement.domain.pet.schedule.dto;

import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

@Data
public class FetchPetScheduleReqDto {
    @PositiveOrZero(message = "valid.petSchedule.id.notNegative")
    private Long id;
}
