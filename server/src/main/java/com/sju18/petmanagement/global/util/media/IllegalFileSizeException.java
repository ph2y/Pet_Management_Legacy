package com.sju18.petmanagement.global.util.media;

public class IllegalFileSizeException extends Exception {
    public IllegalFileSizeException(String filename) {
        super("This file violates file size limit policy: " + filename);
    }
}
