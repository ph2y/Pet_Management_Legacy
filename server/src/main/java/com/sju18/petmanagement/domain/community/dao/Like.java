package com.sju18.petmanagement.domain.community.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sju18.petmanagement.domain.account.dao.Account;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "like_count")
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id;

    // 연관관계 설정 (응답에 포함하지 않음)
    @ManyToOne(targetEntity = Account.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", foreignKey = @ForeignKey(
            name = "fk_like_account_id",
            foreignKeyDefinition = "FOREIGN KEY (account_id) REFERENCES account (account_id) ON DELETE CASCADE"
    ))
    @JsonIgnore
    private Account likedAccount;
    // 응답에 포함할 field
    @Column(name = "account_id", updatable = false, insertable = false)
    private Long likedAccountId;

    // 연관관계 설정 (응답에 포함하지 않음)
    @ManyToOne(targetEntity = Post.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(
            name = "fk_like_post_id",
            foreignKeyDefinition = "FOREIGN KEY (post_id) REFERENCES post (post_id) ON DELETE CASCADE"
    ))
    @JsonIgnore
    private Post likedPost;
    // 응답에 포함할 field
    @Column(name = "post_id", updatable = false, insertable = false)
    private Long likedPostId;

    // 연관관계 설정 (응답에 포함하지 않음)
    @ManyToOne(targetEntity = Comment.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", foreignKey = @ForeignKey(
            name = "fk_like_comment_id",
            foreignKeyDefinition = "FOREIGN KEY (comment_id) REFERENCES comment (comment_id) ON DELETE CASCADE"
    ))
    @JsonIgnore
    private Comment likedComment;
    // 응답에 포함할 field
    @Column(name = "comment_id", updatable = false, insertable = false)
    private Long likedCommentId;
}
