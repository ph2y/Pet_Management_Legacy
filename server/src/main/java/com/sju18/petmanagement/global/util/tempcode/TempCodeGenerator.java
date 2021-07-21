package com.sju18.petmanagement.global.util.tempcode;

import java.security.SecureRandom;
import java.util.Date;

public class TempCodeGenerator {
    public static String generate(char[] charSet, int length) {
        // 임시 코드 생성기
        StringBuffer tempCodeBuffer = new StringBuffer();
        SecureRandom secureRandomNumberGenerator = new SecureRandom();
        secureRandomNumberGenerator.setSeed(new Date().getTime());

        int randomCharIndex = 0;
        for (int i = 0; i < length; i++) {
            randomCharIndex = secureRandomNumberGenerator.nextInt(charSet.length);
            tempCodeBuffer.append(charSet[randomCharIndex]);
        }
        return tempCodeBuffer.toString();
    }
}
