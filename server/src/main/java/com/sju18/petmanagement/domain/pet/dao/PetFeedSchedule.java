package com.sju18.petmanagement.domain.pet.dao;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class PetFeedSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feed_schedule_id")
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Long pet_id;

    @Column
    private LocalDateTime feed_time;
    private String memo;

    @Builder
    public PetFeedSchedule(String username, Long pet_id, LocalDateTime feed_time, String memo) {
        this.username = username;
        this.pet_id = pet_id;
        this.feed_time = feed_time;
        this.memo = memo;
    }
}
