package com.sju18.petmanagement.domain.community.post.dto;

import com.sju18.petmanagement.global.common.DtoMetadata;
import lombok.Data;

@Data
public class CreatePostResDto {
    private DtoMetadata _metadata;
    private Long id;

    // 정상 조회시 사용할 생성자
    public CreatePostResDto(DtoMetadata dtoMetadata, Long postId) {
        this._metadata = dtoMetadata;
        this.id = postId;
    }

    // 오류시 사용할 생성자
    public CreatePostResDto(DtoMetadata dtoMetadata) {
        this._metadata = dtoMetadata;
        this.id = null;
    }
}
