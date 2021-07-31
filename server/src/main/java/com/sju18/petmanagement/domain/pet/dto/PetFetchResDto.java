package com.sju18.petmanagement.domain.pet.dto;

import com.sju18.petmanagement.domain.pet.dao.Pet;
import com.sju18.petmanagement.global.common.DtoMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PetFetchResDto {
    private DtoMetadata _metadata;
    private Long id;
    private String name;
    private String species;
    private String breed;
    private String birth;
    private Boolean yearOnly;
    private Boolean gender;
    private String message;
    private String photoUrl;

    // 정상 조회시 사용할 생성자
    public PetFetchResDto(DtoMetadata dtoMetadata, Pet pet) {
        this._metadata = dtoMetadata;
        this.id = pet.getId();
        this.name = pet.getName();
        this.species = pet.getSpecies();
        this.breed = pet.getBreed();
        this.birth = pet.getBirth().toString();
        this.yearOnly = pet.getYearOnly();
        this.gender = pet.getGender();
        this.message = pet.getMessage();
        this.photoUrl = pet.getPhotoUrl();
    }

    // 오류시 사용할 생성자
    public PetFetchResDto(DtoMetadata dtoMetadata) {
        this._metadata = dtoMetadata;
        this.id = null;
        this.name = null;
        this.species = null;
        this.breed = null;
        this.birth = null;
        this.yearOnly = null;
        this.gender = null;
        this.message = null;
        this.photoUrl = null;
    }
}
