package com.sju18.petmanagement.domain.pet.dao;

import lombok.*;

import javax.persistence.*;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetFeedSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feed_schedule_id")
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String pet_id_list;

    @Column
    private LocalTime feed_time;
    private String memo;
    private Boolean is_turned_on;
}
