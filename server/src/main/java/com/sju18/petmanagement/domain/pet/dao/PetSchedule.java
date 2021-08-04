package com.sju18.petmanagement.domain.pet.dao;

import lombok.*;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pet_schedule_id")
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    @ManyToMany(cascade = {
            CascadeType.DETACH,
            CascadeType.MERGE,
            CascadeType.REFRESH,
            CascadeType.PERSIST
    })
    @JoinTable(name="PetSchedulePetIdList",
            joinColumns = @JoinColumn(name="pet_schedule_id"),
            inverseJoinColumns = @JoinColumn(name="pet_id")
    )
    private List<Pet> petList;

    @Column
    private LocalTime time;
    private String memo;
    private Boolean enabled;

    @Transient
    private String petIdList;
}
