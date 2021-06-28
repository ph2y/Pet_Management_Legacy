package com.sju18.petmanagement.domain.pet.dto;

import com.sju18.petmanagement.domain.pet.dao.Pet;
import lombok.Builder;
import lombok.Data;

@Data
public class PetInfoCreateRequestDto {
    private String name;
    private String birth;
    private String species;
    private String sex;

    @Builder
    public PetInfoCreateRequestDto(String name, String birth, String species, String sex) {
        this.name = name;
        this.birth = birth;
        this.species = species;
        this.sex = sex;
    }

    public Pet toEntity(String username) {
        return Pet.builder()
                .username(username)
                .name(name)
                .birth(birth)
                .species(species)
                .sex(sex)
                .build();
    }
}
