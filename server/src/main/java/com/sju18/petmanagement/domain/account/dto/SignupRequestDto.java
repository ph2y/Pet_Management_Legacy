package com.sju18.petmanagement.domain.account.dto;

import lombok.Data;

@Data
public class SignupRequestDto {
    private String username;
    private String password;
    private String email;
    private String name;
    private String phone;
    private String photo;
    private Boolean marketing;
}
