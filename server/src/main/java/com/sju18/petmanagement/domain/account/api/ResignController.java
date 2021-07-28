package com.sju18.petmanagement.domain.account.api;

import com.sju18.petmanagement.domain.account.dao.AccountRepository;
import com.sju18.petmanagement.domain.account.dto.ResignRequestDto;
import com.sju18.petmanagement.domain.account.dto.ResignResponseDto;
import com.sju18.petmanagement.global.util.storage.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ResignController {
    final AccountRepository accountRepository;
    final FileService fileService;

    @PostMapping("/api/account/resign")
    public ResponseEntity<?> deleteAccount(Authentication authentication, @RequestBody ResignRequestDto resignRequestDto) {
        // 로그인된 현재 사용자 정보 조회
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String currentUserName = userDetails.getUsername();

        // DB 및 파일시스템에서 해당 Account 삭제
        try {
            fileService.deleteAccountFileStorage(accountRepository.findByUsername(currentUserName)
                    .orElseThrow(() -> new UsernameNotFoundException(currentUserName))
                    .getId()
            );
            accountRepository.deleteByUsername(currentUserName);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ResignResponseDto(e.getMessage()));
        }

        return ResponseEntity.ok(new ResignResponseDto("Account delete success"));
    }
}
