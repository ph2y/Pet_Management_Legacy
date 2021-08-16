package com.sju18.petmanagement.domain.pet.schedule.application;

import com.sju18.petmanagement.domain.pet.pet.dao.Pet;
import com.sju18.petmanagement.domain.pet.schedule.dao.PetSchedule;
import com.sju18.petmanagement.domain.pet.schedule.dao.PetScheduleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class PetScheduleCascadeService {
    private final PetScheduleRepository petScheduleRepository;

    @Transactional
    public void deletePetCascadeToPetSchedule(Pet deletingPet) throws IllegalArgumentException {
        // 삭제될 반려동물에 적용되었던 모든 스케줄 정보의 적용되는 반려동물 리스트에서 해당 삭제될 반려동물 제거
        List<PetSchedule> affectedPetScheduleList = petScheduleRepository.findAllByPetListContains(deletingPet);
        for (PetSchedule affectedPetSchedule : affectedPetScheduleList) {
            affectedPetSchedule.getPetList().removeIf(appliedPet -> appliedPet.getId().equals(deletingPet.getId()));
            if (affectedPetSchedule.getPetList().size() == 0) {
                // 삭제될 반려동물을 삭제할 경우 해당 스케줄이 적용되는 반려동물이 없게 되는 경우 해당 스케줄 삭제
                petScheduleRepository.delete(affectedPetSchedule);
            } else {
                petScheduleRepository.save(affectedPetSchedule);
            }
        }
    }
}
