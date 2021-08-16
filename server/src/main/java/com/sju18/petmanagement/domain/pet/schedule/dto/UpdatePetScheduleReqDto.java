package com.sju18.petmanagement.domain.pet.schedule.dto;

import lombok.Data;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

@Data
public class UpdatePetScheduleReqDto {
    @PositiveOrZero(message = "valid.pet.id.notNegative")
    private Long id;
    @Pattern(regexp = "^(0,|[1-9][0-9]{0,9},)*(0|[1-9][0-9]{0,9})$", message = "valid.petSchedule.idList.id")
    private String petIdList;
    @Pattern(regexp = "^(?:(?:([01]?\\d|2[0-3]):)?([0-5]?\\d):)?([0-5]?\\d)$", message = "valid.petSchedule.time.time")
    private String time;
    @Size(max = 200, message = "valid.petSchedule.memo.size")
    private String memo;
    private Boolean enabled;
}
