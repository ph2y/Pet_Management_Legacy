package com.sju18.petmanagement.global.storage;

public class IllegalFileSizeException extends Exception {
    public IllegalFileSizeException(String filename) {
        super("This file violates file size limit policy: " + filename);
    }
}
