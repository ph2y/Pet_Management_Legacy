package com.sju18.petmanagement.global.storage;

public class IllegalFileExtensionException extends Exception {
    public IllegalFileExtensionException(String filename) {
        super("This file violates file extension limit policy: " + filename);
    }
}
