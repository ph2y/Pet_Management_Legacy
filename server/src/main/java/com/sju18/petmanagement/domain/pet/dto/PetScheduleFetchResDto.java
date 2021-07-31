package com.sju18.petmanagement.domain.pet.dto;

import com.sju18.petmanagement.domain.pet.dao.PetSchedule;
import lombok.Data;

@Data
public class PetScheduleFetchResDto {
    private Long id;
    private String pet_id_list;
    private String feed_time;
    private String memo;
    private Boolean is_turned_on;

    public PetScheduleFetchResDto(PetSchedule petFeedSchedule) {
        this.id = petFeedSchedule.getId();
        this.pet_id_list = petFeedSchedule.getPetList().toString();
        this.feed_time = petFeedSchedule.getTime().toString()
                .replace("[", "")
                .replace("]", "");
        this.memo = petFeedSchedule.getMemo();
        this.is_turned_on = petFeedSchedule.getEnable();
    }
}
