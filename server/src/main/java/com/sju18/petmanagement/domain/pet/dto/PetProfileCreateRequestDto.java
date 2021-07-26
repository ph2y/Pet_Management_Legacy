package com.sju18.petmanagement.domain.pet.dto;

import com.sju18.petmanagement.domain.pet.dao.Pet;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PetProfileCreateRequestDto {
    private String name;
    private String species;
    private String breed;
    private String birth;
    private Boolean gender;
    private String message;
    private String photo_url;
}
