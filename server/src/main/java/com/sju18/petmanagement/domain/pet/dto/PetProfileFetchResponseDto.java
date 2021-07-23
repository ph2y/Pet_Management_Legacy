package com.sju18.petmanagement.domain.pet.dto;

import com.sju18.petmanagement.domain.pet.dao.Pet;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PetProfileFetchResponseDto {
    private Long id;
    private String name;
    private String species;
    private String breed;
    private LocalDate birth;
    private Boolean gender;
    private String feed_interval;
    private String memo;
    private String photo_url;

    public PetProfileFetchResponseDto(Pet pet) {
        this.id = pet.getId();
        this.name = pet.getName();
        this.species = pet.getSpecies();
        this.breed = pet.getBreed();
        this.birth = pet.getBirth();
        this.gender = pet.getGender();
        this.feed_interval = pet.getFeed_interval();
        this.memo = pet.getMemo();
        this.photo_url = pet.getPhoto_url();
    }
}
