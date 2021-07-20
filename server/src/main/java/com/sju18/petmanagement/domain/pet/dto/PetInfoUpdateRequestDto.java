package com.sju18.petmanagement.domain.pet.dto;

import lombok.Data;

@Data
public class PetInfoUpdateRequestDto {
    private Long id;
    private String name;
    private String species;
    private String breed;
    private String birth;
    private String gender;
    private String feed_interval;
    private String memo;
    private String photo_url;
}
