package com.sju18.petmanagement.global.storage;

public class EmptyFileException extends Exception {
    public EmptyFileException(String filename) {
        super("This file is empty : " + filename);
    }
}
