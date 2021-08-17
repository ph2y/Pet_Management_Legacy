package com.sju18.petmanagement.domain.map.bookmark.dto;

import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

@Data
public class FetchBookmarkReqDto {
    @PositiveOrZero(message = "valid.bookmark.id.notNegative")
    private Long id;
}
