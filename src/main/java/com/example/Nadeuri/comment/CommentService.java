package com.example.Nadeuri.comment;

import com.example.Nadeuri.board.BoardEntity;
import com.example.Nadeuri.board.BoardRepository;
import com.example.Nadeuri.comment.exception.CommentException;
import com.example.Nadeuri.member.MemberEntity;
import com.example.Nadeuri.member.MemberRepository;
import com.example.Nadeuri.member.exception.MemberException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    public CommentService(CommentRepository commentRepository, BoardRepository boardRepository, MemberRepository memberRepository) {
        this.commentRepository = commentRepository;
        this.boardRepository = boardRepository;
        this.memberRepository = memberRepository;
    }

    // 댓글 생성
    @Transactional
    public CommentDTO createComment(CommentDTO commentDTO) {
        BoardEntity board = boardRepository.findById(commentDTO.getBoardId())
                .orElseThrow(CommentException.NOT_FOUND::get);

        MemberEntity member = memberRepository.findByUserId(commentDTO.getMemberId())
                .orElseThrow(MemberException.NOT_FOUND::get);

        CommentEntity parentComment = null;
        if (commentDTO.getParentCommentId() != null) {
            parentComment = commentRepository.findById(commentDTO.getParentCommentId())
                    .orElseThrow(CommentException.NOT_FOUND::get);
        }

        CommentEntity comment = CommentEntity.builder()
                .board(board)
                .member(member)
                .content(commentDTO.getContent())
                .parentComment(parentComment)
                .build();

        comment = commentRepository.save(comment);

        return CommentDTO.fromEntity(comment);
    }

    // 게시글 ID로 댓글 조회
    @Transactional(readOnly = true)
    public List<CommentDTO> readBoardId(Long boardId) {
        List<CommentEntity> parentComments = commentRepository.Board_Id(boardId);
        List<CommentDTO> commentDTOs = new ArrayList<>();

        for (CommentEntity parentComment : parentComments) {
            commentDTOs.add(buildComment(parentComment));
        }

        return commentDTOs;
    }

    // 댓글 계층 구조로 생성
    private CommentDTO buildComment(CommentEntity comment) {
        List<CommentDTO> replyDTOs = new ArrayList<>();
        if (comment.getReplies() != null) {
            for (CommentEntity reply : comment.getReplies()) {
                replyDTOs.add(buildComment(reply));
            }
        }

        return CommentDTO.builder()
                .id(comment.getId())
                .boardId(comment.getBoard().getId())
                .memberId(comment.getMember().getUserId())
                .content(comment.getContent())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .replies(replyDTOs)
                .build();
    }

    // 유저가 작성한 모든 댓글 조회
    @Transactional(readOnly = true)
    public List<CommentDTO> readMemberId(String userId) {
        List<CommentEntity> comments = commentRepository.Member_UserId(userId);
        List<CommentDTO> commentDTOs = new ArrayList<>();

        for (CommentEntity comment : comments) {
            commentDTOs.add(CommentDTO.fromEntity(comment));
        }

        return commentDTOs;
    }

    // 댓글 수정
    @Transactional
    public CommentDTO updateComment(Long commentId, String content, String userId) {
        CommentEntity comment = check(commentId, userId);
        comment.updateContent(content);

        return CommentDTO.fromEntity(comment);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, String userId) {
        CommentEntity comment = check(commentId, userId);
        commentRepository.delete(comment);
    }

    // 댓글 작성자 검증
    private CommentEntity check(Long commentId, String userId) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(CommentException.NOT_FOUND::get);

        if (!comment.getMember().getUserId().equals(userId)) {
            throw CommentException.NOT_MATCHED_USER.get();
        }

        return comment;
    }
}