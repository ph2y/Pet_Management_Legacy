package com.sju18.petmanagement.global.exception;

import com.sju18.petmanagement.global.util.error.ErrorCode;
import lombok.Getter;

@Getter
public class StorageException extends Exception {
    private final ErrorCode errorCode = ErrorCode.FILESYSTEM;
    public StorageException(String msg) {
        super(msg);
    }
}
