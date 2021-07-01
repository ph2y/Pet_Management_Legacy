package com.sju18.petmanagement.domain.post.dao;

import com.sju18.petmanagement.domain.place.dto.PlaceMarkerUpdateRequestDto;
import com.sju18.petmanagement.domain.post.dto.PostUpdateRequestDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String username;

    @Column
    private String subject;

    @Column
    private String content;

    @Column
    private String image;

    @Column
    private String location;

    @Builder
    public Post(String username, String subject, String content, String image, String location) {
        this.username = username;
        this.subject = subject;
        this.content = content;
        this.image = image;
        this.location = location;
    }

    public Long update(PostUpdateRequestDto requestDto) {
        this.subject = requestDto.getSubject();
        this.content = requestDto.getContent();
        this.image = requestDto.getImage();
        this.location = requestDto.getLocation();

        return this.id;
    }
}
