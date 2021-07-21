package com.sju18.petmanagement.domain.account.dao;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "account_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(unique = true, nullable = false)
    private String email;
    private String nickname;
    @Column(unique = true, nullable = false)
    private String phone;
    private String photo;
    @Column(nullable = false)
    private Boolean marketing;

    public Account(String username, String password, String email, String nickname, String phone, String photo, Boolean marketing) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
        this.phone = phone;
        this.photo = photo;
        this.marketing = marketing;
    }

    public static Account createAccount(String username, String password, String email, String nickname, String phone, String photo, Boolean marketing) {
        return new Account(username,password,email,nickname,phone,photo,marketing);
    }
}
