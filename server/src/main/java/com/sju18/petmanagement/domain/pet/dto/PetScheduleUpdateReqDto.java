package com.sju18.petmanagement.domain.pet.dto;

import lombok.Data;

@Data
public class PetScheduleUpdateReqDto {
    private Long id;
    private String pet_id_list;
    private String feed_time;
    private String memo;
    private Boolean is_turned_on;
}
