package com.sju18.petmanagement.domain.pet.pet.application;

import com.sju18.petmanagement.domain.account.application.AccountService;
import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.pet.pet.dto.*;
import com.sju18.petmanagement.domain.pet.schedule.application.PetScheduleCascadeService;
import com.sju18.petmanagement.domain.pet.pet.dao.Pet;
import com.sju18.petmanagement.domain.pet.pet.dao.PetRepository;
import com.sju18.petmanagement.global.message.MessageConfig;
import com.sju18.petmanagement.global.storage.FileService;
import com.sju18.petmanagement.global.storage.ImageUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtil;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PetService {
    private final MessageSource msgSrc = MessageConfig.getPetMessageSource();
    private final PetRepository petRepository;
    private final PetScheduleCascadeService petScheduleCascadeServ;
    private final AccountService accountServ;
    private final FileService fileServ;

    // CREATE
    @Transactional
    public Long createPet(Authentication auth, CreatePetReqDto reqDto) throws Exception {
        Account owner = accountServ.fetchCurrentAccount(auth);

        // 받은 사용자 정보와 새 입력 정보로 새 반려동물 정보 생성
        Pet pet = Pet.builder()
                .ownername(owner.getUsername())
                .name(reqDto.getName())
                .species(reqDto.getSpecies())
                .breed(reqDto.getBreed())
                .birth(LocalDate.parse(reqDto.getBirth()))
                .yearOnly(reqDto.getYearOnly())
                .gender(reqDto.getGender())
                .message(reqDto.getMessage())
                .build();

        // save(id가 없는 transient 상태의 객체) -> EntityManger.persist() => save
        petRepository.save(pet);

        // 반려동물 파일 저장소 생성
        fileServ.createPetFileStorage(owner.getId(), pet.getId());

        // 반려동물 id 반환
        return pet.getId();
    }

    // READ
    @Transactional(readOnly = true)
    public List<Pet> fetchPetList(Authentication auth) {
        String ownername = accountServ.fetchCurrentAccount(auth).getUsername();

        // 사용자 정보로 반려동물 리스트 인출
        return new ArrayList<>(petRepository.findAllByOwnername(ownername));
    }

    @Transactional(readOnly = true)
    public Pet fetchPetById(Long petId) {
        // 반려동물 고유번호로 반려동물 인출
        return petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException(
                        msgSrc.getMessage("error.pet.notExists", null, Locale.ENGLISH)
                ));
    }

    @Transactional(readOnly = true)
    // id로 인출하되 자신의 반려동물 중에서만 인출됨 (메소드 오버로딩)
    public Pet fetchPetById(Authentication auth, Long petId) {
        String ownername = accountServ.fetchCurrentAccount(auth).getUsername();

        // 사용자 정보로 반려동물 리스트 인출
        return petRepository.findByOwnernameAndId(ownername, petId)
                .orElseThrow(() -> new IllegalArgumentException(
                        msgSrc.getMessage("error.pet.notExists", null, Locale.ENGLISH)
                ));
    }

    public byte[] fetchPetPhoto(Long petId) throws Exception {
        Pet currentPet = petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException(
                        msgSrc.getMessage("error.pet.notExists", null, Locale.ENGLISH)
                ));

        // 사진 파일 인출
        InputStream imageStream = new FileInputStream(ImageUtil.createImageUrl(currentPet.getPhotoUrl(), ImageUtil.THUMBNAIL_IMAGE));
        byte[] fileBinData = IOUtil.toByteArray(imageStream);
        imageStream.close();
        return fileBinData;
    }

    // UPDATE
    @Transactional
    public void updatePet(Authentication auth, UpdatePetReqDto reqDto) {
        // 받은 사용자 정보와 입력 정보로 반려동물 정보 업데이트
        String ownername = accountServ.fetchCurrentAccount(auth).getUsername();
        Pet currentPet = petRepository.findByOwnernameAndId(ownername, reqDto.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        msgSrc.getMessage("error.pet.notExists", null, Locale.ENGLISH)
                ));

        if (reqDto.getName() != null && !reqDto.getName().isEmpty() && !reqDto.getName().equals(currentPet.getName())) {
            currentPet.setName(reqDto.getName());
        }
        if (reqDto.getSpecies() != null && !reqDto.getSpecies().equals(currentPet.getSpecies())) {
            currentPet.setSpecies(reqDto.getSpecies());
        }
        if (reqDto.getBreed() != null && !reqDto.getBreed().equals(currentPet.getBreed())) {
            currentPet.setBreed(reqDto.getBreed());
        }
        if (reqDto.getBirth() != null && !reqDto.getBirth().equals(currentPet.getBirth().toString())) {
            currentPet.setBirth(LocalDate.parse(reqDto.getBirth()));
        }
        if (reqDto.getYearOnly() != null && !reqDto.getYearOnly().equals(currentPet.getYearOnly())) {
            currentPet.setYearOnly(reqDto.getYearOnly());
        }
        if (reqDto.getGender() != null && !reqDto.getGender().equals(currentPet.getGender())) {
            currentPet.setGender(reqDto.getGender());
        }
        if (reqDto.getMessage() != null && !reqDto.getMessage().equals(currentPet.getMessage())) {
            currentPet.setMessage(reqDto.getMessage());
        }

        // save(id가 있는 detached 상태의 객체) -> EntityManger.merge() => update
        petRepository.save(currentPet);
    }

    @Transactional
    public String updatePetPhoto(Authentication auth, UpdatePetPhotoReqDto reqDto) throws Exception {
        // 기존 반려동물 프로필 로드
        Account currentAccount = accountServ.fetchCurrentAccount(auth);
        Pet currentPet = petRepository.findByOwnernameAndId(currentAccount.getUsername(), reqDto.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        msgSrc.getMessage("error.pet.notExists", null, Locale.ENGLISH)
                ));

        // 첨부파일 인출
        MultipartFile uploadedFile = reqDto.getFile();

        // 해당 유저의 계정 스토리지에 프로필 사진 저장
        String fileUrl = null;
        if (uploadedFile != null) {
            fileUrl = fileServ.savePetPhoto(currentAccount.getId(), currentPet.getId(), uploadedFile);

            // 파일정보 DB 데이터 업데이트
            currentPet.setPhotoUrl(fileUrl);
            petRepository.save(currentPet);
        }

        return fileUrl;
    }

    // DELETE
    @Transactional
    public void deletePetPhoto(Authentication auth, DeletePetPhotoReqDto reqDto) throws Exception {
        // 기존 반려동물 프로필 로드
        Account currentAccount = accountServ.fetchCurrentAccount(auth);
        Pet currentPet = petRepository.findByOwnernameAndId(currentAccount.getUsername(), reqDto.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        msgSrc.getMessage("error.pet.notExists", null, Locale.ENGLISH)
                ));

        // 반려동물 프로필에서 photoUrl 컬럼 값 (파일 Path)를 가져와 파일 삭제
        fileServ.deleteImageFile(currentPet.getPhotoUrl());

        // 반려동물 프로필에서 photoUrl 컬럼 null 설정 후 업데이트
        currentPet.setPhotoUrl(null);
        petRepository.save(currentPet);
    }

    @Transactional
    public void deletePet(Authentication auth, DeletePetReqDto reqDto) throws Exception {
        // 받은 사용자 정보와 반려동물 id로 반려동물 정보 삭제
        Account owner = accountServ.fetchCurrentAccount(auth);
        Pet pet = petRepository.findByOwnernameAndId(owner.getUsername(), reqDto.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        msgSrc.getMessage("error.pet.notExists", null, Locale.ENGLISH)
                ));

        accountServ.setRepresentativePetToNull(owner.getId(), pet.getId());
        fileServ.deletePetFileStorage(owner.getId(), pet.getId());
        petScheduleCascadeServ.deletePetCascadeToPetSchedule(pet);
        petRepository.delete(pet);
    }
}
