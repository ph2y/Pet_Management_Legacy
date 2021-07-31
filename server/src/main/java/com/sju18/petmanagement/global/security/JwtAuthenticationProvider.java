package com.sju18.petmanagement.global.security;

import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.account.dao.AccountRepository;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {
    private AccountRepository accountRepository;
    private PasswordEncoder pwEncoder;

    @Override
    // 들어오는 모든 요청에 대한 로그인 여부 검증 (WebSecurityConfig 예외 url 제외)
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        Account account = accountRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));

        if(pwEncoder.matches(account.getPassword(), password)) {
            throw new BadCredentialsException("UnAuthorized");
        }

        return new UsernamePasswordAuthenticationToken(username, password);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
