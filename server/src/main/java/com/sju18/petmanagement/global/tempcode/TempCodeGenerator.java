package com.sju18.petmanagement.global.tempcode;

import java.security.SecureRandom;
import java.util.Date;

public class TempCodeGenerator {
    public static String generate(char[] charSet, int length) {
        // 임시 코드 생성기
        StringBuilder tempCodeBuffer = new StringBuilder();
        SecureRandom secureRandomNumberGenerator = new SecureRandom();
        secureRandomNumberGenerator.setSeed(new Date().getTime());

        int randomCharIndex;
        for (int i = 0; i < length; i++) {
            randomCharIndex = secureRandomNumberGenerator.nextInt(charSet.length);
            tempCodeBuffer.append(charSet[randomCharIndex]);
        }
        return tempCodeBuffer.toString();
    }
}
