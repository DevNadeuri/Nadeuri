package com.example.Nadeuri.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentRequestDTO {

    @NotNull(message = "게시글 ID는 필수입니다.")
    private Long boardId;

    @NotBlank(message = "회원 ID는 필수입니다.")
    private String userId;

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;

    private Long parentCommentId; // 부모 댓글 ID (답글인 경우)

    @Builder
    public CommentRequestDTO(Long boardId, String userId, String content, Long parentCommentId) {
        this.boardId = boardId;
        this.userId = userId;
        this.content = content;
        this.parentCommentId = parentCommentId;
    }
}