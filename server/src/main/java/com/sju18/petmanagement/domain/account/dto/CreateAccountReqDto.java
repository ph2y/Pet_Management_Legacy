package com.sju18.petmanagement.domain.account.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Email;

@Data
public class CreateAccountReqDto {
    @NotBlank(message = "{valid.username.blank}")
    @Size(min = 5, max = 20, message = "{valid.username.size}")
    private String username;

    @NotBlank(message = "{valid.password.blank}")
    @Size(min = 8, max = 20, message = "{valid.password.size}")
    private String password;

    @Email(message = "{valid.email.email}")
    @NotBlank(message = "{valid.email.blank}")
    @Size(max = 50, message = "{valid.email.size}")
    private String email;

    @Size(max = 20, message = "{valid.nickname.size}")
    private String nickname;

    @NotBlank(message = "{valid.phone.blank}")
    @Size(min = 10, max = 11, message = "{valid.phone.size}")
    private String phone;

    @NotBlank(message = "{valid.marketing.blank}")
    private Boolean marketing;

    @Size(max = 200, message = "{valid.userMessage.size}")
    private String userMessage;
}
