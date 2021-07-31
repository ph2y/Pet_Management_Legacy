package com.sju18.petmanagement.global.config.security;

import com.sju18.petmanagement.domain.account.application.AccountLoginService;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    private final AccountLoginService accountLoginServ;
    private final JwtTokenUtil jwtTokenUtil;
    private static final List<String> EXCLUDE_URL = Collections.emptyList();

    // 의존성 주입용 생성자
    public JwtRequestFilter(AccountLoginService accountLoginServ, JwtTokenUtil jwtTokenUtil) {
        this.accountLoginServ = accountLoginServ;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    // 로그인이 요구되는 엔드포인트에 대한 Authorization 검출
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // request 헤더에서 Authorization 필드를 검출
        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        if (requestTokenHeader != null) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (Exception e) {
                logger.warn(e.getMessage());
            }
        } else {
            logger.warn("Authorization Header is null");
        }

        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.accountLoginServ.loadUserByUsername(username);
                if(jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null ,userDetails.getAuthorities());

                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } catch (UsernameNotFoundException e) {
                System.out.println("User Not Found");
            }
        }
        filterChain.doFilter(request,response);
    }

    @Override
    // 검증하지 않을 예외 조건
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return EXCLUDE_URL.stream().anyMatch(exclude -> exclude.equalsIgnoreCase(request.getServletPath()));
    }

}
