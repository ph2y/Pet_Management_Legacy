package com.sju18.petmanagement.domain.community.post.api;

import com.sju18.petmanagement.domain.community.post.application.PostService;
import com.sju18.petmanagement.domain.community.post.dto.FetchPostVideoResDto;
import com.sju18.petmanagement.global.common.DtoMetadata;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;

@RequiredArgsConstructor
@RestController
public class PostStreamingController {
    private static final Logger logger = LogManager.getLogger();
    private final PostService postServ;


    @GetMapping("/api/video/stream")
    public ResponseEntity<?> streamPostVideo(@RequestParam(name = "uri") String videoURI, @RequestHeader(value = "Range", required = false) String httpRangeList) {
        DtoMetadata dtoMetadata;
        ResponseEntity<?> responseEntity;

        try {
            responseEntity = postServ.fetchPostVideo(URLDecoder.decode(videoURI,"UTF8"), httpRangeList);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new FetchPostVideoResDto(dtoMetadata));
        }

        return responseEntity;
    }
}
