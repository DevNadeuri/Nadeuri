package com.example.Nadeuri.comment;

import com.example.Nadeuri.board.BoardEntity;
import com.example.Nadeuri.board.BoardRepository;
import com.example.Nadeuri.member.MemberEntity;
import com.example.Nadeuri.member.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommentControllerTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 댓글_생성_테스트() {
        Long boardId = 1L;
        String memberId = "testUser";
        CommentDTO commentDTO = CommentDTO.builder()
                .boardId(boardId)
                .memberId(memberId)
                .content("테스트 댓글")
                .build();

        BoardEntity board = BoardEntity.builder()
                .id(boardId)
                .build();

        MemberEntity member = MemberEntity.builder()
                .userId(memberId)  // userId로 변경
                .build();

        CommentEntity commentEntity = CommentEntity.builder()
                .id(1L)
                .board(board)
                .member(member)
                .content("테스트 댓글")
                .build();

        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(memberRepository.findByUserId(memberId)).thenReturn(Optional.of(member));  //
        when(commentRepository.save(any(CommentEntity.class))).thenReturn(commentEntity);

        CommentDTO createdComment = commentService.createComment(commentDTO);

        assertNotNull(createdComment);
        assertEquals("테스트 댓글", createdComment.getContent());
        verify(commentRepository, times(1)).save(any(CommentEntity.class));
    }

    @Test
    void 게시글에_달린_댓글_조회_테스트() {
        Long boardId = 1L;

        BoardEntity board = BoardEntity.builder()
                .id(boardId)
                .build();

        MemberEntity member = MemberEntity.builder()
                .userId("testUser")
                .build();

        CommentEntity comment1 = CommentEntity.builder()
                .id(1L)
                .board(board)
                .member(member)
                .content("첫 번째 댓글")
                .build();

        CommentEntity comment2 = CommentEntity.builder()
                .id(2L)
                .board(board)
                .member(member)
                .content("두 번째 댓글")
                .build();

        when(commentRepository.Board_Id(boardId)).thenReturn(Arrays.asList(comment1, comment2));

        List<CommentDTO> comments = commentService.readBoardId(boardId);

        assertEquals(2, comments.size());
        assertEquals("첫 번째 댓글", comments.get(0).getContent());
        assertEquals("두 번째 댓글", comments.get(1).getContent());
        verify(commentRepository, times(1)).Board_Id(boardId);
    }

    @Test
    void 유저가_작성한_댓글_조회_테스트() {
        String memberId = "testUser";  // String 타입으로 변경

        MemberEntity member = MemberEntity.builder()
                .userId(memberId)
                .build();

        BoardEntity board = BoardEntity.builder()
                .id(1L)
                .build();

        CommentEntity comment1 = CommentEntity.builder()
                .id(1L)
                .member(member)
                .board(board)
                .content("유저의 첫 번째 댓글")
                .build();

        CommentEntity comment2 = CommentEntity.builder()
                .id(2L)
                .member(member)
                .board(board)
                .content("유저의 두 번째 댓글")
                .build();

        when(commentRepository.Member_UserId(memberId)).thenReturn(Arrays.asList(comment1, comment2));

        List<CommentDTO> comments = commentService.readMemberId(memberId);

        // 결과 확인
        assertEquals(2, comments.size());
        assertEquals("유저의 첫 번째 댓글", comments.get(0).getContent());
        assertEquals("유저의 두 번째 댓글", comments.get(1).getContent());
        verify(commentRepository, times(1)).Member_UserId(memberId);
    }

    // 댓글 수정 테스트
    @Test
    void 댓글_수정_테스트() {
        Long commentId = 1L;
        String memberId = "testUser";  // String 타입으로 변경
        String updatedContent = "수정된 댓글";

        MemberEntity member = MemberEntity.builder()
                .userId(memberId)
                .build();

        BoardEntity board = BoardEntity.builder()
                .id(1L)
                .build();

        CommentEntity comment = CommentEntity.builder()
                .id(commentId)
                .member(member)
                .board(board)
                .content("이전 댓글")
                .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        CommentDTO updatedComment = commentService.updateComment(commentId, updatedContent, memberId);

        assertNotNull(updatedComment);
        assertEquals(updatedContent, updatedComment.getContent());
        verify(commentRepository, times(1)).findById(commentId);
    }

    @Test
    void 댓글_삭제_테스트() {
        Long commentId = 1L;
        String memberId = "testUser";

        MemberEntity member = MemberEntity.builder()
                .userId(memberId)
                .build();

        CommentEntity comment = CommentEntity.builder()
                .id(commentId)
                .member(member)
                .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        commentService.deleteComment(commentId, memberId);

        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void 답글_생성_테스트() {
        // given
        Long boardId = 1L;
        Long parentCommentId = 1L;
        String memberId = "testUser";

        CommentDTO replyDTO = CommentDTO.builder()
                .boardId(boardId)
                .memberId(memberId)
                .content("답글 내용")
                .parentCommentId(parentCommentId)
                .build();

        BoardEntity board = BoardEntity.builder()
                .id(boardId)
                .build();

        MemberEntity member = MemberEntity.builder()
                .userId(memberId)
                .build();

        CommentEntity parentComment = CommentEntity.builder()
                .id(parentCommentId)
                .board(board)
                .member(member)
                .content("부모 댓글")
                .build();

        CommentEntity replyComment = CommentEntity.builder()
                .id(2L)
                .board(board)
                .member(member)
                .parentComment(parentComment)
                .content("답글 내용")
                .build();

        // 부모 댓글이 이미 저장되어 있는 경우
        when(commentRepository.findById(parentCommentId)).thenReturn(Optional.of(parentComment));
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(memberRepository.findByUserId(memberId)).thenReturn(Optional.of(member));
        when(commentRepository.save(any(CommentEntity.class))).thenReturn(replyComment);

        // when
        CommentDTO createdReply = commentService.createComment(replyDTO);

        // then
        assertNotNull(createdReply);
        assertEquals("답글 내용", createdReply.getContent());
        assertEquals(parentCommentId, createdReply.getParentCommentId());  // 부모 댓글 ID가 올바르게 설정되었는지 확인
        verify(commentRepository, times(1)).save(any(CommentEntity.class));
    }
}