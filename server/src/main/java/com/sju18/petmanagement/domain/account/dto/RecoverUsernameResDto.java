package com.sju18.petmanagement.domain.account.dto;

import com.sju18.petmanagement.global.common.DtoMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecoverUsernameResDto {
    private DtoMetadata _metadata;
    private String username;
}
