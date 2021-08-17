package com.sju18.petmanagement.domain.map.bookmark.dto;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class UpdateBookmarkReqDto {
    @PositiveOrZero(message = "valid.bookmark.id.notNegative")
    Long id;
    @Size(max = 20, message = "valid.bookmark.name.size")
    private String name;
    @Size(max = 250, message = "valid.bookmark.description.size")
    private String description;
}
