package com.sju18.petmanagement.global.util.email;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Calendar;

@Data
@AllArgsConstructor
public class EmailVerificationToken {
    private String email;
    private String authCode;
    private Calendar expireTime;
}
