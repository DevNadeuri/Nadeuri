package com.example.Nadeuri.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    // 특정 게시글 조회
    List<CommentEntity> Board_Id(Long boardId);

    // 특정 유저의 모든 댓글 조회
    List<CommentEntity> Member_UserId(String userId);

}