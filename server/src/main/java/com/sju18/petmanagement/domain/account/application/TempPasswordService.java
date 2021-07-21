package com.sju18.petmanagement.domain.account.application;

import com.sju18.petmanagement.global.util.tempcode.TempCodeGenerator;
import org.springframework.stereotype.Service;

@Service
public class TempPasswordService {
    public String createTempPassword() {
        char[] tempPasswordCharSet = new char[] {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
                'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                '!','@','#','$','%','^','&'
        };
        return TempCodeGenerator.generate(tempPasswordCharSet, 8);
    }
}
