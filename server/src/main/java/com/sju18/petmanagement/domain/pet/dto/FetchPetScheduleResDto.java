package com.sju18.petmanagement.domain.pet.dto;

import com.sju18.petmanagement.domain.pet.dao.PetSchedule;
import com.sju18.petmanagement.global.common.DtoMetadata;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FetchPetScheduleResDto {
    private DtoMetadata _metadata;
    private List<PetSchedule> petScheduleList;

    // 정상 조회시 사용할 생성자
    public FetchPetScheduleResDto(DtoMetadata metadata, List<PetSchedule> petScheduleList) {
        List<Long> applyPetIdList = new ArrayList<>();
        this._metadata = metadata;
        this.petScheduleList = petScheduleList;
    }

    // 오류시 사용할 생성자
    public FetchPetScheduleResDto(DtoMetadata metadata) {
        this._metadata = metadata;
        this.petScheduleList = null;
    }
}
