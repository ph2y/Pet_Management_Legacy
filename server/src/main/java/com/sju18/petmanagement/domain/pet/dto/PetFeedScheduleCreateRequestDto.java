package com.sju18.petmanagement.domain.pet.dto;

import lombok.Data;

@Data
public class PetFeedScheduleCreateRequestDto {
    private String pet_id_list;
    private String feed_time;
    private String memo;
}
