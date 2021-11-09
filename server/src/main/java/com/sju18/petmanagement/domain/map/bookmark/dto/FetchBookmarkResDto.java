package com.sju18.petmanagement.domain.map.bookmark.dto;

import com.sju18.petmanagement.domain.map.bookmark.dao.Bookmark;
import com.sju18.petmanagement.global.common.DtoMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class FetchBookmarkResDto {
    private DtoMetadata _metadata;
    private List<Bookmark> bookmarkList;
}
