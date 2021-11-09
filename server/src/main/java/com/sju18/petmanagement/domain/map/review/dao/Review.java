package com.sju18.petmanagement.domain.map.review.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.map.place.dao.Place;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(targetEntity = Account.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", foreignKey = @ForeignKey(
            name = "fk_review_account_id",
            foreignKeyDefinition = "FOREIGN KEY (account_id) REFERENCES account (account_id) ON DELETE SET NULL"
    ))
    private Account author;

    // 연관관계 설정 (응답에 포함하지 않음)
    @ManyToOne(targetEntity = Place.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", foreignKey = @ForeignKey(
            name = "fk_review_place_id",
            foreignKeyDefinition = "FOREIGN KEY (place_id) REFERENCES place (place_id) ON DELETE CASCADE"
    ))
    @JsonIgnore
    private Place place;
    // 응답에 포함할 field
    @Column(name = "place_id", updatable = false, insertable = false)
    private Long placeId;


    @Lob
    @Column(nullable = false)
    private String contents;
    @Column(nullable = false)
    private Integer rating;
    @Column(nullable = false)
    private LocalDateTime timestamp;
    @Column(nullable = false)
    private Boolean edited;

    @Lob
    @Column
    private String mediaAttachments;
}
