package com.sju18.petmanagement.domain.community.post.dto;

import com.sju18.petmanagement.domain.community.post.dao.Post;
import com.sju18.petmanagement.global.common.DtoMetadata;
import lombok.Data;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Data
public class FetchPostResDto {
    private DtoMetadata _metadata;
    private List<Post> postList;
    private Pageable pageable;
    private Boolean isLast;
    
    // 정상 조회시 사용할 생성자
    public FetchPostResDto(DtoMetadata dtoMetadata, List<Post> postList, Pageable pageable, Boolean isLast) {
        this._metadata = dtoMetadata;
        this.postList = postList;
        this.pageable = pageable;
        this.isLast = isLast;
    }
    
    // 오류시 사용할 생성자
    public FetchPostResDto(DtoMetadata dtoMetadata) {
        this._metadata = dtoMetadata;
        this.postList = null;
        this.pageable = null;
        this.isLast = null;
    }
}
