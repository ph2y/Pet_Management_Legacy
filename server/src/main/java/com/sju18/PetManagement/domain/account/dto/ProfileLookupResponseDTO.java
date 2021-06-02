package com.sju18.PetManagement.domain.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileLookupResponseDTO {
    public ProfileLookupResponseDTO(String message) {
        this.message = message;
    }
    private String message;
    private String username;
    private String email;
    private String name;
    private String phone;
    private String photo;
}
