package com.sju18.petmanagement.domain.account.dto;

import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.global.common.DtoMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FetchAccountResDto {
    private DtoMetadata _metadata;
    private Long id;
    private String username;
    private String email;
    private String phone;
    private Boolean marketing;
    private String nickname;
    private String photoUrl;
    private String userMessage;

    // 정상 조회시 사용할 생성자
    public FetchAccountResDto(DtoMetadata dtoMetadata, Account account) {
        this._metadata = dtoMetadata;
        this.id = account.getId();
        this.username = account.getUsername();
        this.email = account.getEmail();
        this.phone = account.getPhone();
        this.marketing = account.getMarketing();
        this.nickname = account.getNickname();
        this.photoUrl = account.getPhotoUrl();
        this.userMessage = account.getUserMessage();
    }

    // 오류시 사용할 생성자
    public FetchAccountResDto(DtoMetadata dtoMetadata) {
        this._metadata = dtoMetadata;
        this.id = null;
        this.username = null;
        this.email = null;
        this.phone = null;
        this.marketing = null;
        this.nickname = null;
        this.photoUrl = null;
        this.userMessage = null;
    }
}
