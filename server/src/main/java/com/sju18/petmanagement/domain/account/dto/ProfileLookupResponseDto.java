package com.sju18.petmanagement.domain.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileLookupResponseDto {
    public ProfileLookupResponseDto(String message) {
        this.message = message;
    }
    private String message;
    private String username;
    private String email;
    private String nickname;
    private String phone;
    private String photo;
    private Boolean marketing;
    private String userMessage;
}
