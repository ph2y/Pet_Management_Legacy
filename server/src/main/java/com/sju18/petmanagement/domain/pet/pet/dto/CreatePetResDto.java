package com.sju18.petmanagement.domain.pet.pet.dto;

import com.sju18.petmanagement.global.common.DtoMetadata;
import lombok.Data;

@Data
public class CreatePetResDto {
    private DtoMetadata _metadata;
    private Long id;

    // 정상 조회시 사용할 생성자
    public CreatePetResDto(DtoMetadata dtoMetadata, Long petId) {
        this._metadata = dtoMetadata;
        this.id = petId;
    }

    // 오류시 사용할 생성자
    public CreatePetResDto(DtoMetadata dtoMetadata) {
        this._metadata = dtoMetadata;
        this.id = null;
    }
}
