package com.sju18.petmanagement.domain.pet.dto;

import com.sju18.petmanagement.domain.pet.dao.Pet;
import com.sju18.petmanagement.global.common.DtoMetadata;
import lombok.Data;

import java.util.List;

@Data
public class PetFetchResDto {
    private DtoMetadata _metadata;
    private List<Pet> petList;

    // 정상 조회시 사용할 생성자
    public PetFetchResDto(DtoMetadata dtoMetadata, List<Pet> petList) {
        this._metadata = dtoMetadata;
        this.petList = petList;
    }

    // 오류시 사용할 생성자
    public PetFetchResDto(DtoMetadata dtoMetadata) {
        this._metadata = dtoMetadata;
        this.petList = null;
    }
}
