package com.sju18.petmanagement.domain.pet.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class PetInfoResponseDTO {
    private String message;
    private String name;
    private String birth;
    private String species;
    private String sex;

    @Builder
    public PetInfoResponseDTO(String message, String name, String birth, String species, String sex) {
        this.message = message;
        this.name = name;
        this.birth = birth;
        this.species = species;
        this.sex = sex;
    }

    public PetInfoResponseDTO(String errorMessage) {
        this.message = errorMessage;
        this.name = "";
        this.birth = "";
        this.species = "";
        this.sex = "";
    }
}
