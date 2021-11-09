package com.sju18.petmanagement.domain.pet.pet.dao;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pet_id")
    private Long id;

    @Column(nullable = false)
    private String ownername;
    @Column(nullable = false)
    private String name;

    @Column
    private String species;
    private String breed;
    private LocalDate birth;
    private Boolean yearOnly;
    private Boolean gender;
    private String message;
    private String photoUrl;
}
