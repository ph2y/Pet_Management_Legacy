package com.sju18.petmanagement.domain.map.review.api;

import com.sju18.petmanagement.domain.map.review.dao.Review;
import com.sju18.petmanagement.domain.map.review.application.ReviewService;
import com.sju18.petmanagement.domain.map.review.dto.*;
import com.sju18.petmanagement.global.common.DtoMetadata;
import com.sju18.petmanagement.global.message.MessageConfig;
import com.sju18.petmanagement.global.storage.FileMetadata;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@RestController
public class ReviewController {
    private static final Logger logger = LogManager.getLogger();
    private final MessageSource msgSrc = MessageConfig.getMapMessageSource();
    private final ReviewService reviewServ;

    // CREATE
    @PostMapping("/api/review/create")
    public ResponseEntity<?> createReview(Authentication auth, @Valid @RequestBody CreateReviewReqDto reqDto) {
        DtoMetadata dtoMetadata;

        try {
            reviewServ.createReview(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new CreateReviewResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.review.create.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new CreateReviewResDto(dtoMetadata));
    }

    // READ
    @PostMapping("/api/review/fetch")
    public ResponseEntity<?> fetchReview(Authentication auth, @Valid @RequestBody FetchReviewReqDto reqDto) {
        DtoMetadata dtoMetadata;
        List<Review> reviewList;

        try {
            if (reqDto.getId() != null) {
                reviewList = new ArrayList<>();
                reviewList.add(reviewServ.fetchReviewById(reqDto.getId()));
            } else if (reqDto.getPlaceId() != null) {
                reviewList = reviewServ.fetchReviewByPlaceId(reqDto.getPlaceId());
            } else if (reqDto.getAuthorId() != null) {
                reviewList = reviewServ.fetchReviewByAuthor(reqDto.getAuthorId());
            } else {
                reviewList = reviewServ.fetchMyReview(auth);
            }
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new FetchReviewResDto(dtoMetadata, null));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.review.fetch.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new FetchReviewResDto(dtoMetadata, reviewList));
    }

    @PostMapping("/api/review/media/fetch")
    public ResponseEntity<?> fetchReviewMedia(Authentication auth, @Valid @RequestBody FetchReviewMediaReqDto reqDto) {
        DtoMetadata dtoMetadata;
        byte[] fileBinData;
        try {
            fileBinData = reviewServ.fetchReviewMedia(auth, reqDto.getId(), reqDto.getIndex());
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new FetchReviewMediaResDto(dtoMetadata));
        }
        return ResponseEntity.ok(fileBinData);
    }

    // UPDATE
    @PostMapping("/api/review/update")
    public ResponseEntity<?> updateReview(Authentication auth, @Valid @RequestBody UpdateReviewReqDto reqDto) {
        DtoMetadata dtoMetadata;

        try {
            reviewServ.updateReview(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new UpdateReviewResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.review.update.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new UpdateReviewResDto(dtoMetadata));
    }

    @PostMapping("/api/review/media/update")
    public ResponseEntity<?> updateReviewMedia(Authentication auth, @ModelAttribute UpdateReviewMediaReqDto reqDto) {
        DtoMetadata dtoMetadata;
        List<FileMetadata> fileMetadataList;
        try {
            fileMetadataList = reviewServ.updateReviewMedia(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new UpdateReviewMediaResDto(dtoMetadata, null));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.reviewMedia.update.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new UpdateReviewMediaResDto(dtoMetadata, fileMetadataList));
    }

    // DELETE
    @PostMapping("/api/review/delete")
    public ResponseEntity<?> deleteReview(Authentication auth, @Valid @RequestBody DeleteReviewReqDto reqDto) {
        DtoMetadata dtoMetadata;

        try {
            reviewServ.deleteReview(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new DeleteReviewResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.review.delete.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new DeleteReviewResDto(dtoMetadata));
    }
}
