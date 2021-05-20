package com.sju18.PetManagement.domain.account.dao;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "account_id")
    private Long id;

    @Column(unique = true)
    private String username;
    private String password;
    private String email;
    private String name;
    private String phone;
    private String photo;

    public Account(String username, String password, String email, String name, String phone, String photo) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.photo = photo;
    }

    public static Account createAccount(String username, String password, String email, String name, String phone, String photo) {
        return new Account(username,password,email,name,phone,photo);
    }
}
