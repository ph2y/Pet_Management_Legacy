package com.sju18.petmanagement.domain.pet.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class CreatePetReqDto {
    @NotBlank(message = "valid.pet.name.blank")
    @Size(max = 20, message = "valid.pet.name.size")
    private String name;
    @Size(max = 200, message = "valid.pet.species.size")
    private String species;
    @Size(max = 200, message = "valid.pet.breed.size")
    private String breed;
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$|^\\d{4}", message="valid.pet.birth.date")
    private String birth;
    @NotNull(message = "valid.pet.yearOnly.blank")
    private Boolean yearOnly;
    @NotNull(message = "valid.pet.gender.blank")
    private Boolean gender;
    @Size(max = 200, message = "valid.pet.message.size")
    private String message;
}
