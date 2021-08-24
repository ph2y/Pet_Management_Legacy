package com.sju18.petmanagement.domain.map.bookmark.application;

import com.sju18.petmanagement.domain.account.application.AccountService;
import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.map.bookmark.dto.*;
import com.sju18.petmanagement.domain.map.bookmark.dao.Bookmark;
import com.sju18.petmanagement.domain.map.bookmark.dao.BookmarkRepository;
import com.sju18.petmanagement.domain.map.place.application.PlaceService;
import com.sju18.petmanagement.domain.map.place.dao.Place;
import com.sju18.petmanagement.global.message.MessageConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BookmarkService {
    private final MessageSource msgSrc = MessageConfig.getMapMessageSource();
    private final BookmarkRepository bookmarkRepository;
    private final AccountService accountServ;
    private final PlaceService placeServ;

    // CREATE
    @Transactional
    public void createBookmark(Authentication auth, CreateBookmarkReqDto reqDto) throws Exception {
        Account author = accountServ.fetchCurrentAccount(auth);
        Place place = placeServ.fetchPlaceById(reqDto.getPlaceId());

        // 받은 사용자 정보와 입력 정보로 새 장소 즐겨찾기 정보 생성
        Bookmark bookmark = Bookmark.builder()
                .author(author)
                .place(place)
                .name(reqDto.getName())
                .description(reqDto.getDescription())
                .folder(reqDto.getFolder())
                .build();

        // save
        bookmarkRepository.save(bookmark);
    }

    // READ
    @Transactional(readOnly = true)
    public List<Bookmark> fetchBookmarkByAuthor(Authentication auth) {
        Account author = accountServ.fetchCurrentAccount(auth);

        return bookmarkRepository.findAllByAuthor(author);
    }
    @Transactional(readOnly = true)
    public Bookmark fetchBookmarkById(Long bookmarkId) throws Exception {
        return bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.bookmark.notExists", null, Locale.ENGLISH)
                ));
    }
    @Transactional(readOnly = true)
    public List<Bookmark> fetchBookmarkAuthorAndFolder(Authentication auth, String folder) throws Exception {
        Account author = accountServ.fetchCurrentAccount(auth);

        List<Bookmark> bookmarkList = bookmarkRepository.findAllByAuthorAndFolder(author, folder)
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.bookmark.folder.notExists", null, Locale.ENGLISH)
                ));

        return bookmarkList;
    }

    // UPDATE
    @Transactional
    public void updateBookmark(Authentication auth, UpdateBookmarkReqDto reqDto) throws Exception {
        Account author = accountServ.fetchCurrentAccount(auth);

        // 받은 사용자 정보와 입력 정보로 장소 즐겨찾기 정보 수정
        Bookmark currentBookmark = bookmarkRepository.findByAuthorAndId(author, reqDto.getId())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.bookmark.notExists", null, Locale.ENGLISH)
                ));
        if (!reqDto.getName().equals(currentBookmark.getName())) {
            currentBookmark.setName(reqDto.getName());
        }
        if (!reqDto.getDescription().equals(currentBookmark.getDescription())) {
            currentBookmark.setDescription(reqDto.getDescription());
        }
        if(!reqDto.getFolder().equals(currentBookmark.getFolder())) {
            currentBookmark.setFolder(reqDto.getFolder());
        }

        // update
        bookmarkRepository.save(currentBookmark);
    }

    // UPDATE
    @Transactional
    public void updateBookmarkFolder(Authentication auth, UpdateBookmarkFolderReqDto reqDto) throws Exception {
        Account author = accountServ.fetchCurrentAccount(auth);

        // 받은 사용자 정보와 폴더 이름으로 폴더와 폴더 안 모든 즐겨찾기 정보 수정
        List<Bookmark> bookmarkList = bookmarkRepository.findAllByAuthorAndFolder(author, reqDto.getFolder())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.bookmark.folder.notExists", null, Locale.ENGLISH)
                ));
        bookmarkList.stream().map(bookmark -> {
            if(!reqDto.getNewFolder().equals(bookmark.getFolder())) {
                bookmark.setFolder(reqDto.getNewFolder());
            }
            return bookmark;
        }).collect(Collectors.toList());

        // update
        bookmarkRepository.saveAll(bookmarkList);
    }

    // DELETE
    public void deleteBookmark(Authentication auth, DeleteBookmarkReqDto reqDto) throws Exception {
        Account author = accountServ.fetchCurrentAccount(auth);

        // 받은 사용자 정보와 장소 즐겨찾기 id로 장소 즐겨찾기 정보 삭제
        Bookmark bookmark = bookmarkRepository.findByAuthorAndId(author, reqDto.getId())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.bookmark.notExists", null, Locale.ENGLISH)
                ));
        bookmarkRepository.delete(bookmark);
    }

    // DELETE
    public void deleteBookmarkFolder(Authentication auth, DeleteBookmarkFolderReqDto reqDto) throws Exception {
        Account author = accountServ.fetchCurrentAccount(auth);

        // 받은 사용자 정보와 폴더 이름으로 폴더와 폴더 안 모든 즐겨찾기 정보 삭제
        List<Bookmark> bookmarkList = bookmarkRepository.findAllByAuthorAndFolder(author, reqDto.getFolder())
                .orElseThrow(() -> new Exception(
                        msgSrc.getMessage("error.bookmark.folder.notExists", null, Locale.ENGLISH)
                ));
        bookmarkRepository.deleteAll(bookmarkList);
    }
}
