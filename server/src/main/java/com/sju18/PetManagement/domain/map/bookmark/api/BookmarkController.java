package com.sju18.petmanagement.domain.map.bookmark.api;

import com.sju18.petmanagement.domain.map.bookmark.application.BookmarkService;
import com.sju18.petmanagement.domain.map.bookmark.dto.*;
import com.sju18.petmanagement.domain.map.bookmark.dao.Bookmark;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@RestController
public class BookmarkController {
    private static final Logger logger = LogManager.getLogger();
    private final MessageSource msgSrc = MessageConfig.getMapMessageSource();
    private final BookmarkService bookmarkServ;

    // CREATE
    @PostMapping("/api/bookmark/create")
    public ResponseEntity<?> createBookmark(Authentication auth, @Valid @RequestBody CreateBookmarkReqDto reqDto) {
        DtoMetadata dtoMetadata;

        try {
            bookmarkServ.createBookmark(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new CreateBookmarkResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.bookmark.create.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new CreateBookmarkResDto(dtoMetadata));
    }

    // READ
    @PostMapping("/api/bookmark/fetch")
    public ResponseEntity<?> fetchBookmark(Authentication auth, @Valid @RequestBody FetchBookmarkReqDto reqDto) {
        DtoMetadata dtoMetadata;
        List<Bookmark> bookmarkList;

        try {
            if (reqDto.getId() != null) {
                bookmarkList = new ArrayList<>();
                bookmarkList.add(bookmarkServ.fetchBookmarkById(reqDto.getId()));
            } else if(reqDto.getFolder() != null) {
                bookmarkList = bookmarkServ.fetchBookmarkAuthorAndFolder(auth, reqDto.getFolder());
            } else {
                bookmarkList = bookmarkServ.fetchBookmarkByAuthor(auth);
            }
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new FetchBookmarkResDto(dtoMetadata, null));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.bookmark.fetch.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new FetchBookmarkResDto(dtoMetadata, bookmarkList));
    }

    // UPDATE
    @PostMapping("/api/bookmark/update")
    public ResponseEntity<?> updateBookmark(Authentication auth, @Valid @RequestBody UpdateBookmarkReqDto reqDto) {
        DtoMetadata dtoMetadata;

        try {
            bookmarkServ.updateBookmark(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new UpdateBookmarkResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.bookmark.update.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new UpdateBookmarkResDto(dtoMetadata));
    }

    // UPDATE
    @PostMapping("/api/bookmark/folder/update")
    public ResponseEntity<?> updateBookmarkFolder(Authentication auth, @Valid @RequestBody UpdateBookmarkFolderReqDto reqDto) {
        DtoMetadata dtoMetadata;

        try {
            bookmarkServ.updateBookmarkFolder(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new UpdateBookmarkFolderResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.bookmark.folder.update.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new UpdateBookmarkFolderResDto(dtoMetadata));
    }

    // DELETE
    @PostMapping("/api/bookmark/delete")
    public ResponseEntity<?> deleteBookmark(Authentication auth, @Valid @RequestBody DeleteBookmarkReqDto reqDto) {
        DtoMetadata dtoMetadata;

        try {
            bookmarkServ.deleteBookmark(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new DeleteBookmarkResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.bookmark.delete.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new DeleteBookmarkResDto(dtoMetadata));
    }

    // DELETE
    @PostMapping("/api/bookmark/folder/delete")
    public ResponseEntity<?> deleteBookmarkFolder(Authentication auth, @Valid @RequestBody DeleteBookmarkFolderReqDto reqDto) {
        DtoMetadata dtoMetadata;

        try {
            bookmarkServ.deleteBookmarkFolder(auth, reqDto);
        } catch (Exception e) {
            logger.warn(e.toString());
            dtoMetadata = new DtoMetadata(e.getMessage(), e.getClass().getName());
            return ResponseEntity.status(400).body(new DeleteBookmarkFolderResDto(dtoMetadata));
        }
        dtoMetadata = new DtoMetadata(msgSrc.getMessage("res.bookmark.folder.delete.success", null, Locale.ENGLISH));
        return ResponseEntity.ok(new DeleteBookmarkFolderResDto(dtoMetadata));
    }
}
