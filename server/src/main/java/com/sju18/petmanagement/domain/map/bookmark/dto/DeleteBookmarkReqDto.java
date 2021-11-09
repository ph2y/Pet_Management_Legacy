package com.sju18.petmanagement.domain.map.bookmark.dto;

import lombok.Data;

import javax.validation.constraints.PositiveOrZero;

@Data
public class DeleteBookmarkReqDto {
    @PositiveOrZero(message = "valid.bookmark.id.notNegative")
    Long id;
}
