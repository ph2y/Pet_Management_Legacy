package com.sju18.petmanagement.global.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DtoMetadata {
    private Boolean status;
    private Integer code;
    private String message;
    private String exception;
}
