package com.sju18.petmanagement.domain.pet.application;

import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.pet.dao.Pet;
import com.sju18.petmanagement.domain.pet.dao.PetRepository;
import com.sju18.petmanagement.global.storage.FileService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PetCascadeService {
    private final PetRepository petRepository;
    private final PetScheduleCascadeService petScheduleCascadeServ;
    private final FileService fileServ;

    public void deleteAccountCascadeToPet(Account deletingAccount) throws Exception {
        // 삭제하려는 사용자가 가진 모든 펫 삭제
        List<Pet> petList = petRepository.findAllByOwnername(deletingAccount.getUsername());
        for (Pet pet : petList) {
            fileServ.deletePetFileStorage(deletingAccount.getId(), pet.getId());
            petScheduleCascadeServ.deletePetCascadeToPetSchedule(pet);
            petRepository.delete(pet);
        }
    }
}
