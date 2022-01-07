package com.sju18.petmanagement.domain.community.post.dao;

import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.pet.pet.dao.Pet;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(targetEntity = Account.class, fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "account_id")
    private Account author;

    @ManyToOne(targetEntity = Pet.class, fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "pet_id")
    private Pet pet;

    @Lob
    @Column
    private String contents;
    @Column(nullable = false)
    private LocalDateTime timestamp;
    @Column(nullable = false)
    private Boolean edited;

    @Column(name = "tag_list")
    private String serializedHashTags;

    @Column
    private String disclosure;
    private Double geoTagLat;
    private Double geoTagLong;
    @Lob
    private String imageAttachments;
    @Lob
    private String videoAttachments;
    @Lob
    private String fileAttachments;
}
