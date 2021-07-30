package com.sju18.petmanagement.global.exception;

import com.sju18.petmanagement.global.util.error.ErrorCode;
import lombok.Getter;

@Getter
public class DbException extends Exception {
    private final ErrorCode errorCode = ErrorCode.DATABASE;
    public DbException(String msg) {
        super(msg);
    }
}
