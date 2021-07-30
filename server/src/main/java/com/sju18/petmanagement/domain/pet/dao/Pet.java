package com.sju18.petmanagement.domain.pet.dao;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pet_id")
    private Long id;

    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String name;

    @Column
    private String species;
    private String breed;
    private LocalDate birth;
    private Boolean year_only;
    private Boolean gender;
    private String message;
    private String photo_url;

    @Builder
    public Pet(String username, String name, String species, String breed, LocalDate birth, Boolean year_only, Boolean gender, String message, String photo_url) {
        this.username = username;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.birth = birth;
        this.year_only = year_only;
        this.gender = gender;
        this.message = message;
        this.photo_url = photo_url;
    }
}
