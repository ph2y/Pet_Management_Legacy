package com.sju18.petmanagement.domain.pet.dao;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String username;

    @Column
    private String name;

    @Column
    private String birth;

    @Column
    private String species;

    @Column
    private String sex;

    @Builder
    public Pet(String username, String name, String birth, String species, String sex) {
        this.username = username;
        this.name = name;
        this.birth = birth;
        this.species = species;
        this.sex = sex;
    }
}
