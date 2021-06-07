package com.sju18.petmanagement.domain.account.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Permission {

    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    private final String value;

}
