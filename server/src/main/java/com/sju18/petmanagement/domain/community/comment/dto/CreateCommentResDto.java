package com.sju18.petmanagement.domain.community.comment.dto;

import com.sju18.petmanagement.global.common.DtoMetadata;
import lombok.Data;

@Data
public class CreateCommentResDto {
    private DtoMetadata _metadata;
    private Long id;

    // 정상 조회시 사용할 생성자
    public CreateCommentResDto(DtoMetadata dtoMetadata, Long commentId) {
        this._metadata = dtoMetadata;
        this.id = commentId;
    }

    // 오류시 사용할 CreateCommentResDto
    public CreateCommentResDto(DtoMetadata dtoMetadata) {
        this._metadata = dtoMetadata;
        this.id = null;
    }
}
