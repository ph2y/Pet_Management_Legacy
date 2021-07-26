package com.sju18.petmanagement.domain.pet.dto;

import lombok.Data;

@Data
public class PetFeedScheduleCreateRequestDto {
    private Long pet_id;
    private String feed_time;
    private String memo;
}
