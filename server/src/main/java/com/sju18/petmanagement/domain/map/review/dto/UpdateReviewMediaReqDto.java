package com.sju18.petmanagement.domain.map.review.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class UpdateReviewMediaReqDto {
    @PositiveOrZero(message = "valid.review.id.notNegative")
    Long id;
    @Size(max = 10, message = "valid.review.media.count")
    List<MultipartFile> fileList;
}
