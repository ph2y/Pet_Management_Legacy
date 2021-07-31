package com.sju18.petmanagement.domain.account.dto;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class CreateAccountReqDto {
    @NotBlank(message = "valid.account.username.blank")
    @Size(min = 5, max = 20, message = "valid.account.username.size")
    private String username;

    @NotBlank(message = "valid.account.password.blank")
    @Size(min = 8, max = 20, message = "valid.account.password.size")
    private String password;

    @Email(message = "valid.account.email.email")
    @NotBlank(message = "valid.account.email.blank")
    @Size(max = 50, message = "valid.account.email.size")
    private String email;

    @NotBlank(message = "valid.account.phone.blank")
    @Size(min = 12, max = 13, message = "valid.account.phone.size")
    @Pattern(regexp = "/(^02|^\\d{3})-(\\d{3}|\\d{4})-\\d{4}/", message = "valid.account.phone.phone")
    private String phone;

    @Size(max = 20, message = "valid.account.nickname.size")
    private String nickname;

    @NotNull(message = "valid.account.marketing.null")
    private Boolean marketing;

    @Size(max = 200, message = "valid.account.userMessage.size")
    private String userMessage;
}
