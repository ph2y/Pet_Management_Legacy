package com.sju18.petmanagement.domain.pet.dto;

import com.sju18.petmanagement.domain.pet.dao.Pet;
import lombok.Builder;
import lombok.Data;

@Data
public class PetProfileCreateRequestDto {
    private String name;
    private String species;
    private String breed;
    private String birth;
    private String gender;
    private String feed_interval;
    private String memo;
    private String photo_url;

    @Builder
    public PetProfileCreateRequestDto(String name, String species, String breed, String birth, String gender, String feed_interval, String memo, String photo_url) {
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.birth = birth;
        this.gender = gender;
        this.feed_interval = feed_interval;
        this.memo = memo;
        this.photo_url = photo_url;
    }

    public Pet toEntity(String username) {
        return Pet.builder()
                .username(username)
                .name(name)
                .species(species)
                .breed(breed)
                .birth(birth)
                .gender(gender)
                .feed_interval(feed_interval)
                .memo(memo)
                .photo_url(photo_url)
                .build();
    }
}
