package com.sju18.petmanagement.domain.community.post.dto;

import lombok.Data;
import javax.validation.constraints.PositiveOrZero;

@Data
public class DeletePostFileReqDto {
    @PositiveOrZero(message = "valid.post.id.notNegative")
    private Long id;
    private String fileType;
}

