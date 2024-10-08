package com.example.Nadeuri.comment;

import com.example.Nadeuri.member.security.auth.CustomUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // 댓글 생성 - 부모 댓글이 있을 경우 해당 정보도 함께 처리
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommentDTO> createComment(
            @Valid @RequestBody CommentRequestDTO commentRequestDTO,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        String userId = extractUserId(user);

        // 새로운 댓글 또는 답글 생성
        CommentDTO newComment = CommentDTO.builder()
                .memberId(userId)
                .boardId(commentRequestDTO.getBoardId())
                .content(commentRequestDTO.getContent())
                .parentCommentId(commentRequestDTO.getParentCommentId())
                .build();

        CommentDTO createdComment = commentService.createComment(newComment);
        return ResponseEntity.ok(createdComment);
    }

    // 게시글에 달린 모든 댓글 조회 - 계층형 구조로 조회
    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<CommentDTO>> readBoard(
            @PathVariable Long boardId) {
        List<CommentDTO> comments = commentService.readBoardId(boardId);
        return ResponseEntity.ok(comments);
    }

    // 사용자가 작성한 모든 댓글 조회 - 누구나 조회 가능
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<CommentDTO>> readMember(
            @PathVariable String memberId) {
        List<CommentDTO> comments = commentService.readMemberId(memberId);
        return ResponseEntity.ok(comments);
    }

    // 댓글 수정 - 자신의 댓글이거나 ADMIN 경우만 가능
    @PutMapping("/{commentId}")
    @PreAuthorize("hasRole('ADMIN') or #user.getName() == authentication.name")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequestDTO commentRequestDTO,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        String userId = extractUserId(user);

        // 업데이트된 댓글 생성
        CommentDTO updatedComment = commentService.updateComment(commentId, commentRequestDTO.getContent(), userId);
        return ResponseEntity.ok(updatedComment);
    }

    // 댓글 삭제 - 자신의 댓글이거나 ADMIN 경우만 가능
    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasRole('ADMIN') or @commentService.checkOwner(#commentId, authentication.name)")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        String userId = extractUserId(user);
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    // 사용자 ID 추출 메서드
    private String extractUserId(CustomUserPrincipal user) {
        return user.getName();
    }
}