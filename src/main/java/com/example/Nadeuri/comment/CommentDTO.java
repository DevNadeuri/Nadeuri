package com.example.Nadeuri.comment;

import com.example.Nadeuri.board.BoardEntity;
import com.example.Nadeuri.member.MemberEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class CommentDTO {

    private Long id;
    private Long boardId;
    private String memberId;
    private String content;
    private Long parentCommentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentDTO> replies;

    @Builder
    public CommentDTO(Long id, Long boardId, String memberId, String content, Long parentCommentId, LocalDateTime createdAt, LocalDateTime updatedAt, List<CommentDTO> replies) {
        this.id = id;
        this.boardId = boardId;
        this.memberId = memberId;
        this.content = content;
        this.parentCommentId = parentCommentId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.replies = replies;
    }

    // entity -> dto 변환
    public static CommentDTO fromEntity(CommentEntity commentEntity) {
        List<CommentDTO> replyDTOs = new ArrayList<>();
        if (commentEntity.getReplies() != null) {
            for (CommentEntity reply : commentEntity.getReplies()) {
                replyDTOs.add(fromEntity(reply));
            }
        }

        return CommentDTO.builder()
                .id(commentEntity.getId())
                .boardId(commentEntity.getBoard().getId())
                .memberId(commentEntity.getMember().getUserId())
                .content(commentEntity.getContent())
                .parentCommentId(commentEntity.getParentComment() != null ? commentEntity.getParentComment().getId() : null)
                .createdAt(commentEntity.getCreatedAt())
                .updatedAt(commentEntity.getUpdatedAt())
                .replies(replyDTOs)
                .build();
    }

    // dto -> entity 변환
    public CommentEntity toEntity(BoardEntity board, MemberEntity member, CommentEntity parentComment) {
        return CommentEntity.builder()
                .id(this.id)
                .board(board)
                .member(member)
                .content(this.content)
                .parentComment(parentComment)
                .build();
    }
}