package com.sju18.petmanagement.domain.community.follow.dto;

import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.global.common.DtoMetadata;
import lombok.Data;

import java.util.List;

@Data
public class FetchFollowingResDto {
    private DtoMetadata _metadata;
    private List<Account> followingList;

    // 정상 조회시 사용할 생성자
    public FetchFollowingResDto(DtoMetadata metadata, List<Account> followingList) {
        this._metadata = metadata;
        this.followingList = followingList;
    }

    // 오류시 사용할 생성자
    public FetchFollowingResDto(DtoMetadata metadata) {
        this._metadata = metadata;
        this.followingList = null;
    }
}
