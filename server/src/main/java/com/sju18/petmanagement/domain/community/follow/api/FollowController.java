package com.sju18.petmanagement.domain.community.follow.api;

import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.community.follow.application.FollowService;
import com.sju18.petmanagement.domain.community.follow.dao.Follow;
import com.sju18.petmanagement.domain.community.follow.dto.*;
import com.sju18.petmanagement.global.common.DtoMetadata;
import com.sju18.petmanagement.global.message.MessageConfig;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class FollowController {
    private static final Logger logger = LogManager.getLogger();
    private final MessageSource msgSrc = MessageConfig.getCommunityMessageSource();
    private final FollowService followServ;

    @PostMapping("/api/community/follow/create")
    public ResponseEntity<?> createFollow(Authentication auth, @Valid @RequestBody CreateFollowReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            followServ.createFollow(auth, reqDto);
        }
        catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new CreateFollowResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.follow.create.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new CreateFollowResDto(dtoMetadata));
    }

    // 현재 사용자가 팔로잉하고 있는 계정 리스트 Fetch
    @PostMapping("api/community/follower/fetch")
    public ResponseEntity<?> fetchFollower(Authentication auth, @Valid @RequestBody FetchFollowerReqDto reqDto) {
        DtoMetadata dtoMetadata;
        final List<Account> followerList;

        try {
            followerList = followServ.fetchFollower(auth).stream().map(Follow::getFollower).collect(Collectors.toList());
        }
        catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new FetchFollowerResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.follower.fetch.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new FetchFollowerResDto(dtoMetadata, followerList));
    }

    // 현재 사용자를 팔로우하고 있는 계정 리스트 Fetch
    @PostMapping("api/community/following/fetch")
    public ResponseEntity<?> fetchFollowing(Authentication auth, @Valid @RequestBody FetchFollowingReqDto reqDto) {
        DtoMetadata dtoMetadata;
        final List<Account> followingList;

        try {
            followingList = followServ.fetchFollowing(auth).stream().map(Follow::getFollowing).collect(Collectors.toList());
        }
        catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new FetchFollowingResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.following.fetch.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new FetchFollowingResDto(dtoMetadata, followingList));
    }

    @PostMapping("api/community/follow/delete")
    public ResponseEntity<?> deleteFollow(Authentication auth, @Valid @RequestBody DeleteFollowReqDto reqDto) {
        DtoMetadata dtoMetadata;
        try {
            followServ.deleteFollow(auth, reqDto);
        }
        catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new DeleteFollowResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.follow.delete.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new DeleteFollowResDto(dtoMetadata));
    }
}
