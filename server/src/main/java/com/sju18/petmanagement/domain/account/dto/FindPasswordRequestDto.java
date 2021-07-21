package com.sju18.petmanagement.domain.account.dto;

import lombok.Data;

@Data
public class FindPasswordRequestDto {
    private String username;
    private String code;
}
