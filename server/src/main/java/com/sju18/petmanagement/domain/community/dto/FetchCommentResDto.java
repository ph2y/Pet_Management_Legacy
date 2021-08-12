package com.sju18.petmanagement.domain.community.dto;

import com.sju18.petmanagement.domain.community.dao.Comment;
import com.sju18.petmanagement.global.common.DtoMetadata;
import lombok.Data;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Data
public class FetchCommentResDto {
    private DtoMetadata _metadata;
    private List<Comment> commentList;
    private Pageable pageable;
    private Boolean isLast;

    // 정상 조회시 사용할 생성자
    public FetchCommentResDto(DtoMetadata dtoMetadata, List<Comment> commentList, Pageable pageable, Boolean isLast) {
        this._metadata = dtoMetadata;
        this.commentList = commentList;
        this.pageable = pageable;
        this.isLast = isLast;
    }

    // 오류시 사용할 생성자
    public FetchCommentResDto(DtoMetadata dtoMetadata) {
        this._metadata = dtoMetadata;
        this.commentList = null;
        this.pageable = null;
        this.isLast = null;
    }
}
