package com.sju18.petmanagement.domain.map.bookmark.dao;

import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.map.place.dao.Place;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    private Long id;

    @ManyToOne(targetEntity = Account.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", foreignKey = @ForeignKey(
            name = "fk_bookmark_account_id",
            foreignKeyDefinition = "FOREIGN KEY (account_id) REFERENCES account (account_id) ON DELETE CASCADE"
    ))
    private Account author;

    @ManyToOne(targetEntity = Place.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "place_id", foreignKey = @ForeignKey(
            name = "fk_bookmark_place_id",
            foreignKeyDefinition = "FOREIGN KEY (place_id) REFERENCES place (place_id) ON DELETE CASCADE"
    ))
    private Place place;

    @Column
    private String name;
    private String description;
    private String folder;
}
