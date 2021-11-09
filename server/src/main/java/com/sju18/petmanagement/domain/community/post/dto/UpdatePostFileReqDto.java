package com.sju18.petmanagement.domain.community.post.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class UpdatePostFileReqDto {
    @PositiveOrZero(message = "valid.post.id.notNegative")
    Long id;
    @Size(max = 10, message = "valid.post.file.count")
    List<MultipartFile> fileList;
}
