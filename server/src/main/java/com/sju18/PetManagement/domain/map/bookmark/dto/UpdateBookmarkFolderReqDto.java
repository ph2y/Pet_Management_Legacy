package com.sju18.petmanagement.domain.map.bookmark.dto;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class UpdateBookmarkFolderReqDto {
    @Size(max = 10, message = "valid.bookmark.folder.name.size")
    String folder;
    @Size(max = 10, message = "valid.bookmark.folder.name.size")
    String newFolder;
}
