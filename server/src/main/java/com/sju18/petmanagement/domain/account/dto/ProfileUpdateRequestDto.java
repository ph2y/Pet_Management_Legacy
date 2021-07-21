package com.sju18.petmanagement.domain.account.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequestDto {
    private String username;
    private String email;
    private String nickname;
    private String phone;
    private String photo;
    private Boolean marketing;
}
