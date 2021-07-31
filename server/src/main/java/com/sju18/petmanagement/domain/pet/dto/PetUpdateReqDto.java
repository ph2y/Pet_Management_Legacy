package com.sju18.petmanagement.domain.pet.dto;

import lombok.Data;

@Data
public class PetUpdateReqDto {
    private Long id;
    private String name;
    private String species;
    private String breed;
    private String birth;
    private Boolean year_only;
    private Boolean gender;
    private String message;
    private String photo_url;
}
