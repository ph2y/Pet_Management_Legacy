package com.sju18.petmanagement.domain.community.dao;

import com.sju18.petmanagement.domain.account.dao.Account;
import lombok.*;
import javax.persistence.*;

@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"follower_id", "following_id"}
                )
        }
)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // follower: following 객체에게 follow 되는 객체
    @ManyToOne
    @JoinColumn(name = "follower_id")
    Account follower;

    // following: following 객체를 follow 하는 객체
    @ManyToOne
    @JoinColumn(name = "following_id")
    Account following;
}
