package com.sju18.petmanagement.domain.pet.schedule.dao;

import com.sju18.petmanagement.domain.pet.pet.dao.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetScheduleRepository extends JpaRepository<PetSchedule, Long> {
    List<PetSchedule> findAllByUsername(String username);
    List<PetSchedule> findAllByPetListContains(Pet appliedPet);
    Optional<PetSchedule> findByUsernameAndId(String username, Long id);
}
