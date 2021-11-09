package com.sju18.petmanagement.domain.account.dto;

import lombok.Data;

@Data
public class LoginReqDto {
    private String username;
    private String password;
}
