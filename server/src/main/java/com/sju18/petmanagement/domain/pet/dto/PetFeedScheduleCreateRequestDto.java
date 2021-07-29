package com.sju18.petmanagement.domain.pet.dto;

import lombok.Data;
import java.util.Set;

@Data
public class PetFeedScheduleCreateRequestDto {
    private Set<Long> pet_id;
    private String feed_time;
    private String memo;
}
