package com.sju18.petmanagement.global.util.media;

public class EmptyFileException extends Exception {
    public EmptyFileException(String filename) {
        super("This file is empty : " + filename);
    }
}
