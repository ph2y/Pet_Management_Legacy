package com.sju18.petmanagement.domain.pet.dto;

import com.sju18.petmanagement.domain.pet.dao.PetFeedSchedule;
import lombok.Data;

@Data
public class PetFeedScheduleFetchResponseDto {
    private Long pet_id;
    private String feed_time;
    private String memo;

    public PetFeedScheduleFetchResponseDto(PetFeedSchedule petFeedSchedule) {
        this.pet_id = petFeedSchedule.getPet_id();
        this.feed_time = petFeedSchedule.getFeed_time().toString();
        this.memo = petFeedSchedule.getMemo();
    }
}
