package com.sju18.petmanagement.domain.map.bookmark.dto;

import lombok.Data;

import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

@Data
public class FetchBookmarkReqDto {
    @PositiveOrZero(message = "valid.bookmark.id.notNegative")
    private Long id;
    @Size(max = 10, message = "valid.bookmark.folder.name.size")
    String folder;
}
