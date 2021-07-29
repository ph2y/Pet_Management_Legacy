package com.sju18.petmanagement.domain.pet.dto;

import com.sju18.petmanagement.domain.pet.dao.PetFeedSchedule;
import lombok.Data;

@Data
public class PetFeedScheduleFetchResponseDto {
    private Long id;
    private String pet_id_list;
    private String feed_time;
    private String memo;
    private Boolean is_turned_on;

    public PetFeedScheduleFetchResponseDto(PetFeedSchedule petFeedSchedule) {
        this.id = petFeedSchedule.getId();
        this.pet_id_list = petFeedSchedule.getPet_id_list();
        this.feed_time = petFeedSchedule.getFeed_time().toString();
        this.memo = petFeedSchedule.getMemo();
        this.is_turned_on = petFeedSchedule.getIs_turned_on();
    }
}
