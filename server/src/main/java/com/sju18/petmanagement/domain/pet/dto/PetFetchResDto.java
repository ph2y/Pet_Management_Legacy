package com.sju18.petmanagement.domain.pet.dto;

import com.sju18.petmanagement.domain.pet.dao.Pet;
import lombok.Data;

@Data
public class PetFetchResDto {
    private Long id;
    private String name;
    private String species;
    private String breed;
    private String birth;
    private Boolean year_only;
    private Boolean gender;
    private String message;
    private String photo_url;

    public PetFetchResDto(Pet pet) {
        this.id = pet.getId();
        this.name = pet.getName();
        this.species = pet.getSpecies();
        this.breed = pet.getBreed();
        this.birth = pet.getBirth().toString();
        this.year_only = pet.getYear_only();
        this.gender = pet.getGender();
        this.message = pet.getMessage();
        this.photo_url = pet.getPhoto_url();
    }
}
