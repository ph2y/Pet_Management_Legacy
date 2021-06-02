package com.sju18.PetManagement.domain.account.application;

import com.sju18.PetManagement.domain.account.dao.Account;
import com.sju18.PetManagement.domain.account.dao.AccountRepository;
import com.sju18.PetManagement.domain.account.dao.Permission;

import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Set;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    // 스프링 시큐리티 유저 정보 로드
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(Permission.USER.getValue()));
//        아래 소스는 특정 조건에서 어드민 계정으로 넣고 싶을 때 조건을 수정하여 사용하면 됨.
//        아래 예시는 username이 "sju18"인 경우에 admin권한을 준다는것임.
//        if (username.equals("sju18")) {
//            grantedAuthorities.add(new SimpleGrantedAuthority(Permission.ADMIN.getValue()));
//        }

        return new User(account.getUsername(), account.getPassword(), grantedAuthorities);
    }

    // 유저네임과 비밀번호로 스프링 시큐리티 인증
    public Account authenticateByUsernameAndPassword(String username, String password) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        if(!passwordEncoder.matches(password, account.getPassword())) {
            throw new BadCredentialsException("Password not matched");
        }

        return account;
    }

}