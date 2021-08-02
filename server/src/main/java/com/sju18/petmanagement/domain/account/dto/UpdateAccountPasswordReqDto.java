package com.sju18.petmanagement.domain.account.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UpdateAccountPasswordReqDto {
    @NotBlank(message = "valid.account.password.blank")
    @Size(min = 8, max = 20, message = "valid.account.password.size")
    private String password;
}
