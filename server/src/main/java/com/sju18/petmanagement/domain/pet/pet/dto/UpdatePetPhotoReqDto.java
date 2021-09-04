package com.sju18.petmanagement.domain.pet.pet.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.PositiveOrZero;

@Data
public class UpdatePetPhotoReqDto {
    @PositiveOrZero(message = "valid.pet.id.notNegative")
    private Long id;
    MultipartFile file;
}
