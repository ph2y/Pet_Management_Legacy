package com.sju18.petmanagement.domain.map.review.application;

import com.google.gson.Gson;
import com.sju18.petmanagement.domain.account.application.AccountService;
import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.map.review.dao.Review;
import com.sju18.petmanagement.domain.map.review.dao.ReviewRepository;
import com.sju18.petmanagement.domain.map.place.application.PlaceService;
import com.sju18.petmanagement.domain.map.place.dao.Place;
import com.sju18.petmanagement.domain.map.review.dto.CreateReviewReqDto;
import com.sju18.petmanagement.domain.map.review.dto.DeleteReviewReqDto;
import com.sju18.petmanagement.domain.map.review.dto.UpdateReviewMediaReqDto;
import com.sju18.petmanagement.domain.map.review.dto.UpdateReviewReqDto;
import com.sju18.petmanagement.global.message.MessageConfig;
import com.sju18.petmanagement.global.storage.FileMetadata;
import com.sju18.petmanagement.global.storage.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Service
public class ReviewService {
    private final MessageSource msgSrc = MessageConfig.getMapMessageSource();
    private final ReviewRepository reviewRepository;
    private final AccountService accountServ;
    private final PlaceService placeServ;
    private final FileService fileServ;

    // CREATE
    @Transactional
    public void createReview(Authentication auth, CreateReviewReqDto reqDto) throws Exception {
        Account author = accountServ.fetchCurrentAccount(auth);
        Place place = placeServ.fetchPlaceById(reqDto.getPlaceId());

        // 받은 사용자 정보와 입력 정보로 새 장소 즐겨찾기 정보 생성
        Review review = Review.builder()
                .author(author)
                .place(place)
                .placeId(place.getId())
                .contents(reqDto.getContents())
                .rating(reqDto.getRating())
                .timestamp(LocalDateTime.now())
                .edited(false)
                .build();

        // save
        reviewRepository.save(review);

        // 리뷰 파일 저장소 생성
        fileServ.createReviewFileStorage(review.getId());
    }

    // READ
    @Transactional(readOnly = true)
    public List<Review> fetchReviewByPlaceId(Long placeId) {
        return reviewRepository.findAllByPlaceId(placeId);
    }
    @Transactional(readOnly = true)
    public List<Review> fetchReviewByAuthor(Long authorId) throws Exception {
        Account author = accountServ.fetchAccountById(authorId);
        return reviewRepository.findAllByAuthor(author);
    }
    @Transactional(readOnly = true)
    public List<Review> fetchMyReview(Authentication auth) {
        Account author = accountServ.fetchCurrentAccount(auth);
        return reviewRepository.findAllByAuthor(author);
    }
    @Transactional(readOnly = true)
    public Review fetchReviewById(Long reviewId) throws Exception {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.review.notExists", null, Locale.ENGLISH)
                ));
    }

    public byte[] fetchReviewMedia(Authentication auth, Long reviewId, Integer fileIndex) throws Exception {
        Account author = accountServ.fetchCurrentAccount(auth);
        Review currentReview = reviewRepository.findByAuthorAndId(author, reviewId)
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.review.notExists", null, Locale.ENGLISH)
                ));

        // 미디어 파일 인출
        return fileServ.readFileFromFileMetadataListJson(currentReview.getMediaAttachments(), fileIndex);
    }

    // UPDATE
    @Transactional
    public void updateReview(Authentication auth, UpdateReviewReqDto reqDto) throws Exception {
        Account author = accountServ.fetchCurrentAccount(auth);

        // 받은 사용자 정보와 입력 정보로 장소 즐겨찾기 정보 수정
        Review currentReview = reviewRepository.findByAuthorAndId(author, reqDto.getId())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.review.notExists", null, Locale.ENGLISH)
                ));
        if (!reqDto.getContents().equals(currentReview.getContents())) {
            currentReview.setContents(reqDto.getContents());
        }
        if (!reqDto.getRating().equals(currentReview.getRating())) {
            currentReview.setRating(reqDto.getRating());
        }
        currentReview.setEdited(true);

        // save
        reviewRepository.save(currentReview);
    }

    @Transactional
    public List<FileMetadata> updateReviewMedia(Authentication auth, UpdateReviewMediaReqDto reqDto) throws Exception {
        // 기존 리뷰 정보 로드
        Account author = accountServ.fetchCurrentAccount(auth);
        Review currentReview = reviewRepository.findByAuthorAndId(author, reqDto.getId())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.review.notExists", null, Locale.ENGLISH)
                ));

        // 첨부파일 인출
        List<MultipartFile> uploadedFileList = reqDto.getFileList();

        // 해당 리뷰의 미디어 스토리지에 미디어 파일 저장
        List<FileMetadata> mediaFileMetadataList = null;
        if (uploadedFileList.size() != 0) {
            mediaFileMetadataList = fileServ.saveReviewAttachments(reqDto.getId(), uploadedFileList);

            // 파일정보 DB 데이터 업데이트
            currentReview.setMediaAttachments(new Gson().toJson(mediaFileMetadataList));
            reviewRepository.save(currentReview);
        }
        return mediaFileMetadataList;
    }

    // DELETE
    public void deleteReview(Authentication auth, DeleteReviewReqDto reqDto) throws Exception {
        Account author = accountServ.fetchCurrentAccount(auth);

        // 받은 사용자 정보와 장소 즐겨찾기 id로 장소 즐겨찾기 정보 삭제
        Review review = reviewRepository.findByAuthorAndId(author, reqDto.getId())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.review.notExists", null, Locale.ENGLISH)
                ));
        // 리뷰 파일 저장소 삭제
        fileServ.deleteReviewFileStorage(review.getId());
        reviewRepository.delete(review);
    }
}
