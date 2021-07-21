package com.sju18.petmanagement.global.util.email;

import com.sju18.petmanagement.global.util.tempcode.TempCodeGenerator;

import java.util.*;

public class EmailVerificationService {
    private final static List<EmailVerificationToken> verificationTokenList = new LinkedList<EmailVerificationToken>();

    public static String createAuthCode(String email) {
        // 이메일 인증요청 리스트에 새로운 인증요청정보 추가
        char[] authCodeCharSet = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        String authCode = TempCodeGenerator.generate(authCodeCharSet, 8);

        // 해당 이메일로 기존에 발송한 인증코드가 있을 경우 무효화
        verificationTokenList.removeIf((token) -> (token.getEmail().equals(email)));

        // 이메일 인증번호 만료시간 산출
        Calendar expiredTime = Calendar.getInstance();
        expiredTime.setTime(new Date());
        expiredTime.add(Calendar.MINUTE, 10);
        
        // 이메일 인증요청 리스트에 추가
        verificationTokenList.add(new EmailVerificationToken(email, authCode, expiredTime));

        return authCode;
    }

    public static boolean checkAuthCode(String email, String authCode) {
        // 이메일 인증코드 검사
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTime(new Date());

        for (EmailVerificationToken token: verificationTokenList) {
            if (
                token.getEmail().equals(email) &&
                token.getAuthCode().equals(authCode) &&
                token.getExpireTime().compareTo(currentTime) > 0
            ) {
                verificationTokenList.remove(token);
                return true;
            }
        }
        return false;
    }

    public static void deleteExpiredAuthCode() {
        // 이메일 인증요청 리스트 정리 (현재 시간부로 만료시간이 된 데이터 삭제)
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTime(new Date());
        verificationTokenList.removeIf((token) -> (token.getExpireTime().compareTo(currentTime) <= 0));
    }
}
