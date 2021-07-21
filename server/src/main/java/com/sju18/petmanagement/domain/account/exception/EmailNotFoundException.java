package com.sju18.petmanagement.domain.account.exception;

public class EmailNotFoundException extends Exception {
    public EmailNotFoundException(String email) {
        super("There is no account who registered by this email : " + email);
    }
}
