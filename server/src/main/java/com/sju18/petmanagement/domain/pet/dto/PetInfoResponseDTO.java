package com.sju18.petmanagement.domain.pet.dto;

import com.sju18.petmanagement.domain.pet.dao.Pet;
import lombok.Builder;
import lombok.Data;

@Data
public class PetInfoResponseDTO {
    private Long id;
    private String name;
    private String birth;
    private String species;
    private String sex;

    public PetInfoResponseDTO(Pet pet) {
        this.id = pet.getId();
        this.name = pet.getName();
        this.birth = pet.getBirth();
        this.species = pet.getSpecies();
        this.sex = pet.getSex();
    }
}
