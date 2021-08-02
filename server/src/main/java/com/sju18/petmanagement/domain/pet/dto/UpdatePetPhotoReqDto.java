package com.sju18.petmanagement.domain.pet.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.PositiveOrZero;

@Data
public class UpdatePetPhotoReqDto {
    @PositiveOrZero
    Long id;
    MultipartFile file;
}
