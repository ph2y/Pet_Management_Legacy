package com.sju18.petmanagement.domain.account.application;

import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.account.dao.AccountRepository;
import com.sju18.petmanagement.domain.account.dao.Permission;
import com.sju18.petmanagement.global.util.message.MessageConfig;

import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Service
@AllArgsConstructor
public class AccountLoginService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder pwEncoder;
    private final MessageSource msgSrc = MessageConfig.getAccountMessageSource();

    // 스프링 시큐리티 유저 권한 정보 로드
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(Permission.USER.getValue()));

        return new User(account.getUsername(), account.getPassword(), grantedAuthorities);
    }

    // 유저네임과 비밀번호로 스프링 시큐리티 인증
    public Account loginByCredential(String username, String password) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        
        // 비밀번호 확인
        if(!pwEncoder.matches(password, account.getPassword())) {
            throw new BadCredentialsException(msgSrc.getMessage("error.login.fail", null, Locale.ENGLISH));
        }

        return account;
    }
}
