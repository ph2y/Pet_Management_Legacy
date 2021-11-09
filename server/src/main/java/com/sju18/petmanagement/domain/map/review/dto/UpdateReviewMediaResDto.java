package com.sju18.petmanagement.domain.map.review.dto;

import com.sju18.petmanagement.global.common.DtoMetadata;
import com.sju18.petmanagement.global.storage.FileMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UpdateReviewMediaResDto {
    private DtoMetadata _metadata;
    private List<FileMetadata> fileMetadataList;
}
