package com.sju18.petmanagement.domain.community.comment.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sju18.petmanagement.domain.account.dao.Account;
import com.sju18.petmanagement.domain.community.post.dao.Post;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(targetEntity = Account.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", foreignKey = @ForeignKey(
            name = "fk_comment_account_id",
            foreignKeyDefinition = "FOREIGN KEY (account_id) REFERENCES account (account_id) ON DELETE SET NULL"
    ))
    private Account author;

    // 연관관계 설정 (응답에 포함하지 않음)
    @ManyToOne(targetEntity = Post.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(
            name = "fk_comment_post_id",
            foreignKeyDefinition = "FOREIGN KEY (post_id) REFERENCES post (post_id) ON DELETE CASCADE"
    ))
    @JsonIgnore
    private Post post;
    // 응답에 포함할 field
    @Column(name="post_id", updatable=false, insertable=false)
    private Long postId;

    // 연관관계 설정 (응답에 포함하지 않음)
    @ManyToOne(targetEntity = Comment.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id", foreignKey = @ForeignKey(
            name = "fk_comment_parent_comment_id",
            foreignKeyDefinition = "FOREIGN KEY (parent_comment_id) REFERENCES comment (comment_id) ON DELETE CASCADE"
    ))
    @JsonIgnore
    private Comment parentComment;
    // 응답에 포함할 field
    @Column(name="parent_comment_id", updatable = false, insertable = false)
    private Long parentCommentId;

    @Column(nullable = false)
    private Integer childCommentCnt;
    @Lob
    @Column(nullable = false)
    private String contents;
    @Column(nullable = false)
    private LocalDateTime timestamp;
    @Column(nullable = false)
    private Boolean edited;
}
