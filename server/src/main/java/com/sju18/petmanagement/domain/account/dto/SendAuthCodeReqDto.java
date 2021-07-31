package com.sju18.petmanagement.domain.account.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class SendAuthCodeReqDto {
    @Email(message = "valid.account.email.email")
    @NotBlank(message = "valid.account.email.blank")
    @Size(max = 50, message = "valid.account.email.size")
    private String email;
}
