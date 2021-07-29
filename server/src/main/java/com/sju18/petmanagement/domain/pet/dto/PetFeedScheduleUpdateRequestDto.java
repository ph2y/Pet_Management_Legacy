package com.sju18.petmanagement.domain.pet.dto;

import lombok.Data;
import java.util.Set;

@Data
public class PetFeedScheduleUpdateRequestDto {
    private Long id;
    private Set<Long> pet_id;
    private String feed_time;
    private String memo;
    private Boolean is_turned_on;
}
