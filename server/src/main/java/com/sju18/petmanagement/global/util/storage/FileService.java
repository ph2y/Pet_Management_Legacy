package com.sju18.petmanagement.global.util.media;

import com.sju18.petmanagement.domain.account.dao.AccountRepository;
import com.sju18.petmanagement.domain.pet.dao.PetRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.aspectj.util.FileUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FileService {
    // TODO: Repository에 직접 억세스하지 않도록 accountService, petService 리팩토링
    private final AccountRepository accountRepository;
    private final PetRepository petRepository;
    private final String storageRootPath = "E:\\TempDev\\Pet-Management\\storage";

    // 특정 사용자 데이터 폴더 경로 조회
    public Path getAccountFileStoragePath(Long accountId) {
        return Paths.get(storageRootPath, "accounts", "account_" + accountId);
    }
    // 특정 반려동물 데이터 폴더 경로 조회
    public Path getPetFileStoragePath(Long petId) throws Exception {
        Long accountId = accountRepository.findByUsername(
                petRepository.findById(petId)
                        .orElseThrow(() -> new Exception("Pet entity not found"))
                        .getUsername()
        ).orElseThrow(() -> new Exception("Account entity not found")).getId();
        return Paths.get(getAccountFileStoragePath(accountId).toString(), "pets", "pet_" + petId);
    }
    // 특정 게시물 데이터 폴더 경로 조회
    public Path getPostFileStoragePath(Long postId) {
        return Paths.get(storageRootPath, "community", "post", "post_" + postId);
    }
    // 특정 게시물 댓글 데이터 폴더 경로 조회 - TODO: 장래 커뮤니티 기능 구현시 같이 구현예정
    // TODO: getCommentFileStoragePath(Long commentId) 구현

    // 사용자 데이터 폴더 생성
    public void createAccountFileStorage(Long accountId) throws Exception {
        Path accountProfileStorage = getAccountFileStoragePath(accountId);
        Files.createDirectories(accountProfileStorage);
        FileUtil.makeNewChildDir(accountProfileStorage.toFile(), "pets");
    }
    // 사용자 데이터 폴더 삭제
    public void deleteAccountFileStorage(Long accountId) throws Exception {
        Path accountProfileStorage = getAccountFileStoragePath(accountId);
        FileUtils.deleteDirectory(accountProfileStorage.toFile());
        Files.delete(accountProfileStorage);
    }

    // 반려동물 폴더 생성
    public void createPetFileStorage(Long petId) throws Exception {
        Path petProfileStorage = getPetFileStoragePath(petId);
        Files.createDirectories(petProfileStorage);
    }
    // 반려동물 폴더 삭제
    public void deletePetFileStorage(Long petId) throws Exception {
        Path petProfileStorage = getPetFileStoragePath(petId);
        FileUtils.deleteDirectory(petProfileStorage.toFile());
        Files.delete(petProfileStorage);
    }

    // 게시물 데이터 폴더 생성
    public void createPostFileStorage(Long postId) throws Exception {
        Path postAttachedFileStorage = getPostFileStoragePath(postId);
        Files.createDirectories(postAttachedFileStorage);
        FileUtil.makeNewChildDir(postAttachedFileStorage.toFile(), "comments");
    }
    // 게시물 데이터 폴더 삭제
    public void deletePostFileStorage(Long postId) throws Exception {
        Path postAttachedFileStorage = getPostFileStoragePath(postId);
        FileUtils.deleteDirectory(postAttachedFileStorage.toFile());
        Files.delete(postAttachedFileStorage);
    }

    // 사용자 프로필 사진 저장
    public String saveAccountProfilePhoto(Long accountId, MultipartFile uploadedFile) throws Exception {
        // 업로드 파일 저장 파일명
        String fileName = "profile_photo" + FilenameUtils.getExtension(uploadedFile.getOriginalFilename());
        // 업로드 파일 저장 경로
        Path savePath = getAccountFileStoragePath(accountId);
        // 업로드 가능한 확장자
        String[] acceptableExtensions = new String[]{
                "jpg","png","jpeg", "gif", "webp"
        };
        // 업로드 파일 용량 제한 (5MB)
        long fileSizeLimit = 5000000;

        // 파일 유효성 검사
        checkFileValidity(savePath, uploadedFile, acceptableExtensions, fileSizeLimit);

        // 파일 저장
        uploadedFile.transferTo(savePath.resolve(fileName));

        return savePath.resolve(fileName).toString();
    }
    
    // 애완동물 프로필 사진 저장
    public String savePetProfilePhoto(Long petId, MultipartFile uploadedFile) throws Exception {
        // 업로드 파일 저장 파일명
        String fileName = "pet_profile_photo" + FilenameUtils.getExtension(uploadedFile.getOriginalFilename());
        // 업로드 파일 저장 경로
        Path savePath = getPetFileStoragePath(petId);
        // 업로드 가능한 확장자
        String[] acceptableExtensions = new String[]{
                "jpg","png","jpeg", "gif", "webp"
        };
        // 업로드 파일 용량 제한 (5MB)
        long fileSizeLimit = 5000000;

        // 파일 유효성 검사
        checkFileValidity(savePath, uploadedFile, acceptableExtensions, fileSizeLimit);
        
        // 파일 저장
        uploadedFile.transferTo(savePath.resolve(fileName));

        return savePath.resolve(fileName).toString();
    }
    
    // 게시물 첨부파일 저장
    public JSONArray savePostAttachments(Long postId, List<MultipartFile> uploadedFiles) throws Exception {
        // 업로드 다중파일 저장 경로
        Path savePath = getPostFileStoragePath(postId);
        // 업로드 가능한 확장자
        String[] acceptableExtensions = new String[]{
                "jpg","png","jpeg", "gif", "webp", "mp3", "flac", "mp4", "webm"
        };
        // 업로드 개별 파일 용량 제한 (100MB)
        long fileSizeLimit = 100000000;
        // 업로드 파일 갯수 제한
        int fileCountLimit = 10;
        //
        JSONArray fileMetaDataList = new JSONArray();

        if (uploadedFiles.size() > fileCountLimit) {
            throw new IllegalFileCountException(fileCountLimit, uploadedFiles.size());
        }

        for (MultipartFile uploadedFile : uploadedFiles) {
            try {
                // 업로드 파일 저장 파일명 설정
                String fileName = "post_" + postId + "_" + uploadedFile.getOriginalFilename();
                // 파일 유효성 검사
                checkFileValidity(savePath, uploadedFile, acceptableExtensions, fileSizeLimit);
                // 파일 저장
                uploadedFile.transferTo(savePath.resolve(fileName));
                // 파일 메타데이터 정보 생성
                JSONObject fileMetaData = new JSONObject();
                fileMetaData.put("fileName", fileName);
                fileMetaData.put("fileSize", uploadedFile.getSize());
                fileMetaData.put("fileType", "null");
                fileMetaData.put("fileUrl", savePath.resolve(fileName));

                fileMetaDataList.put(fileMetaData);
            } catch (Exception e) {
                // 업로드 실패시 이미 업로드된 파일 삭제
                // TODO: 게시물 수정시 기존 첨부파일에 대한 예외처리 필요
                FileUtils.deleteDirectory(savePath.toFile());
                throw e;
            }
        }

        return fileMetaDataList;
    }

    // 파일 검증 로직
    public void checkFileValidity(Path savePath, MultipartFile uploadedFile, String[] acceptableExtensions, Long fileSizeLimit) throws Exception {
        // 업로드 파일 원본 파일명
        String originalFileName = uploadedFile.getOriginalFilename();

        // 저장할 데이터 디렉토리 존재 여부 검사
        if (!savePath.toFile().exists()) {
            throw new FileNotFoundException("Directory not exist");
        }
        // 빈 파일 검사
        if (uploadedFile.isEmpty()) {
            throw new EmptyFileException(originalFileName);
        }
        // 확장자 없는 파일 검사
        else if (FilenameUtils.getExtension(originalFileName) == null) {
            throw new IllegalFileExtensionException(null);
        }
        // 파일 확장자 적합성 검사
        else if (Arrays.stream(acceptableExtensions).noneMatch(
                extension -> FilenameUtils.getExtension(originalFileName).equals(extension)
        )) {
            throw new IllegalFileExtensionException(originalFileName);
        }
        // 파일 크기 적합성 검사
        else if (uploadedFile.getSize() > fileSizeLimit) {
            throw new IllegalFileSizeException(originalFileName);
        }
    }
}
