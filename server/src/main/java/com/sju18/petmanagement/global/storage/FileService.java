package com.sju18.petmanagement.global.storage;

import com.sju18.petmanagement.global.message.MessageConfig;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.aspectj.util.FileUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class FileService {
    private final MessageSource msgSrc = MessageConfig.getStorageMessageSource();
    private final String storageRootPath = "C:\\Users\\Komputer\\Download\\Pet-Management\\storage";

    // 특정 사용자 데이터 폴더 경로 조회
    public Path getAccountFileStoragePath(Long accountId) {
        return Paths.get(storageRootPath, "accounts", "account_" + accountId);
    }
    // 특정 반려동물 데이터 폴더 경로 조회
    public Path getPetFileStoragePath(Long ownerAccountId, Long petId) {
        return Paths.get(getAccountFileStoragePath(ownerAccountId).toString(), "pets", "pet_" + petId);
    }
    // 특정 게시물 데이터 폴더 경로 조회
    public Path getPostFileStoragePath(Long postId) {
        return Paths.get(storageRootPath, "community", "post", "post_" + postId);
    }
    // 특정 게시물 댓글 데이터 폴더 경로 조회 - TODO: 장래 커뮤니티 기능 구현시 같이 구현예정
    // TODO: getCommentFileStoragePath(Long commentId) 구현

    // 사용자 데이터 폴더 생성
    public void createAccountFileStorage(Long accountId) throws Exception {
        Path accountStorage = getAccountFileStoragePath(accountId);
        Files.createDirectories(accountStorage);
        FileUtil.makeNewChildDir(accountStorage.toFile(), "pets");
    }
    // 사용자 데이터 폴더 삭제
    public void deleteAccountFileStorage(Long accountId) throws Exception {
        Path accountStorage = getAccountFileStoragePath(accountId);
        FileUtils.deleteDirectory(accountStorage.toFile());
    }

    // 반려동물 폴더 생성
    public void createPetFileStorage(Long ownerAccountId, Long petId) throws Exception {
        Path petStorage = getPetFileStoragePath(ownerAccountId, petId);
        Files.createDirectories(petStorage);
    }
    // 반려동물 폴더 삭제
    public void deletePetFileStorage(Long ownerAccountId, Long petId) throws Exception {
        Path petStorage = getPetFileStoragePath(ownerAccountId, petId);
        FileUtils.deleteDirectory(petStorage.toFile());
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
    }

    // 사용자 프로필 사진 저장
    public String saveAccountPhoto(Long accountId, MultipartFile uploadedFile) throws Exception {
        // 업로드 파일 저장 파일명
        String fileName = "account_profile_photo." + FileUtils.getExtension(Objects.requireNonNull(uploadedFile.getOriginalFilename()));
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
    public String savePetPhoto(Long ownerAccountId, Long petId, MultipartFile uploadedFile) throws Exception {
        // 업로드 파일 저장 파일명
        String fileName = "pet_profile_photo." + FileUtils.getExtension(Objects.requireNonNull(uploadedFile.getOriginalFilename()));
        // 업로드 파일 저장 경로
        Path savePath = getPetFileStoragePath(ownerAccountId, petId);
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
            throw new Exception(msgSrc.getMessage("error.file.count", null, Locale.ENGLISH));
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
            throw new FileNotFoundException(msgSrc.getMessage("error.dir.notExist", null, Locale.ENGLISH));
        }
        // 빈 파일 검사
        if (uploadedFile.isEmpty()) {
            throw new Exception(msgSrc.getMessage("error.file.empty", new String[]{originalFileName}, Locale.ENGLISH));
        }
        // 파일 확장자 적합성 검사
        else if (Arrays.stream(acceptableExtensions).noneMatch(
                extension -> FileUtils.getExtension(Objects.requireNonNull(originalFileName)).equals(extension)
        )) {
            throw new Exception(msgSrc.getMessage("error.file.extension.valid", new String[]{originalFileName}, Locale.ENGLISH));
        }
        // 파일 크기 적합성 검사
        else if (uploadedFile.getSize() > fileSizeLimit) {
            throw new Exception(msgSrc.getMessage("error.file.size", new String[]{originalFileName}, Locale.ENGLISH));
        }
    }
}
