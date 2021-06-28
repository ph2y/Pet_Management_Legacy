package com.sju18.petmanagement.domain.pet.dto;

import lombok.Data;

@Data
public class PetInfoUpdateRequestDto {
    private Long id;
    private String name;
    private String birth;
    private String species;
    private String sex;
}
