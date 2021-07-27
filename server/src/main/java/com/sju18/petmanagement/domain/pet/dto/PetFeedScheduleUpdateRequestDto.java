package com.sju18.petmanagement.domain.pet.dto;

import lombok.Data;

@Data
public class PetFeedScheduleUpdateRequestDto {
    private Long id;
    private Long pet_id;
    private String feed_time;
    private String memo;
    private Boolean is_turned_on;
}
