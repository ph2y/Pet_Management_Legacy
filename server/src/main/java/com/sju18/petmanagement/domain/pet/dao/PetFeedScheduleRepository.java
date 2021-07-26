package com.sju18.petmanagement.domain.pet.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetFeedScheduleRepository extends JpaRepository<PetFeedSchedule, Long> {
    List<PetFeedSchedule> findAllByUsername(String username);
    Optional<PetFeedSchedule> findByUsernameAndId(String username, Long id);
}
