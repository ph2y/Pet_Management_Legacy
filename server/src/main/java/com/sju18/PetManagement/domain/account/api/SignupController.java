package com.sju18.PetManagement.domain.account.api;

import com.sju18.PetManagement.domain.account.dto.SignupRequestDTO;
import lombok.RequiredArgsConstructor;

import com.sju18.PetManagement.domain.account.dao.Account;
import com.sju18.PetManagement.domain.account.dao.AccountRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SignupController {

    final AccountRepository accountRepository;
    final PasswordEncoder encode;

    @PostMapping("/api/account/signup")
    public String registerAccount(@RequestBody SignupRequestDTO signupRequestDTO) {
        accountRepository.save(
                Account.createAccount(
                        signupRequestDTO.getUsername(),
                        encode.encode(signupRequestDTO.getPassword()),
                        signupRequestDTO.getEmail(),
                        signupRequestDTO.getName(),
                        signupRequestDTO.getPhone(),
                        signupRequestDTO.getPhoto()
                )
        );
        return "success";
    }

}


