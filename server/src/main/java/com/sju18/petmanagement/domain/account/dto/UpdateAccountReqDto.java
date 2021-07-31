package com.sju18.petmanagement.domain.account.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class UpdateAccountReqDto {
    @Email(message = "valid.email.email")
    @Size(max = 50, message = "valid.email.size")
    private String email;
    @Size(min = 12, max = 13, message = "valid.phone.size")
    private String phone;
    @Size(max = 20, message = "valid.nickname.size")
    private String nickname;
    private Boolean marketing;
    @Size(max = 200, message = "valid.userMessage.size")
    private String userMessage;
}
