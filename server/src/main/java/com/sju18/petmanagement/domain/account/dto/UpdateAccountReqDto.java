package com.sju18.petmanagement.domain.account.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class UpdateAccountReqDto {
    @Email(message = "valid.account.email.email")
    @Size(max = 50, message = "valid.account.email.size")
    private String email;
    @Size(min = 12, max = 13, message = "valid.account.phone.size")
    @Pattern(regexp = "(^02|^\\d{3})-(\\d{3}|\\d{4})-\\d{4}", message = "valid.account.phone.phone")
    private String phone;
    @Size(max = 20, message = "valid.account.nickname.size")
    private String nickname;
    private String photoUrl;
    private Boolean marketing;
    @Size(max = 200, message = "valid.account.userMessage.size")
    private String userMessage;
}
