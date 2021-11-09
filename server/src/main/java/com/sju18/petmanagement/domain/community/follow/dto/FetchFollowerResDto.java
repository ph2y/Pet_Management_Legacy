package com.sju18.petmanagement.domain.community.follow.dto;

import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.global.common.DtoMetadata;
import lombok.Data;

import java.util.List;

@Data
public class FetchFollowerResDto {
    private DtoMetadata _metadata;
    private List<Account> followerList;

    // 정상 조회시 사용할 생성자
    public FetchFollowerResDto(DtoMetadata metadata, List<Account> followerList) {
        this._metadata = metadata;
        this.followerList = followerList;
    }

    // 오류시 사용할 생성자
    public FetchFollowerResDto(DtoMetadata metadata) {
        this._metadata = metadata;
        this.followerList = null;
    }
}
