package com.sju18.petmanagement.domain.account.application;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;

@Service
public class TempPasswordService {
    public String createTempPassword() {
        char[] tempPasswordCharSet = new char[] {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
                'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                '!','@','#','$','%','^','&'
        };
        int tempPasswordLength = 8;
        StringBuffer tempPasswordBuffer = new StringBuffer();
        SecureRandom secureRandomNumberGenerator = new SecureRandom();
        secureRandomNumberGenerator.setSeed(new Date().getTime());

        int randomCharIndex = 0;
        for (int i = 0; i < tempPasswordLength; i++) {
            randomCharIndex = secureRandomNumberGenerator.nextInt(tempPasswordCharSet.length);
            tempPasswordBuffer.append(tempPasswordCharSet[randomCharIndex]);
        }

        return tempPasswordBuffer.toString();
    }
}
