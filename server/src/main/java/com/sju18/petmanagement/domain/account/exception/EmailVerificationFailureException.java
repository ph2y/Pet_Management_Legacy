package com.sju18.petmanagement.domain.account.exception;

public class EmailVerificationFailureException extends Exception {
    public EmailVerificationFailureException(String email) {
        super("The verification was failed of this email : " + email);
    }
}
