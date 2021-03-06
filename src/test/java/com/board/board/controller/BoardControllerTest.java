package com.board.board.controller;

import com.board.board.domain.Board;
import com.board.board.domain.Reply;
import com.board.board.domain.oauth.Role;
import com.board.board.domain.oauth.User;
import com.board.board.dto.Board.BoardListDto;
import com.board.board.dto.Board.BoardPostDto;
import com.board.board.dto.Board.BoardSearchCondition;
import com.board.board.dto.reply.ReplyPostDto;
import com.board.board.dto.reply.ReplySaveDto;
import com.board.board.mapper.Reply.ReplySaveMapper;
import com.board.board.repository.board.BoardRepository;
import com.board.board.repository.oauth.UserRepository;
import com.board.board.service.BoardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class BoardControllerTest {
    @Autowired
    BoardController boardController;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    ReplyController replyController;

    @Autowired
    BoardService boardService;

    @Autowired
    EntityManager entityManager;

    @Autowired
    ReplySaveMapper replySaveMapper;

    @Test
    public void 페이징테스트() throws Exception {
        //given
        User user = new User(1L, "찬", "fbcks97@naver.com", "picture", Role.GUEST);
        userRepository.save(user);
        for(int i=0; i<100; i++){
            Board board = new Board((long) i, "제목"+i, "내용"+i, user);
            boardRepository.save(board);
        }


        //when
        BoardSearchCondition condition = new BoardSearchCondition();
        condition.setTitle("제목1");
        System.out.println(condition);

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<BoardListDto> results = boardController.findBoardList(condition, pageRequest);

        //then
        System.out.println("~~~~~~~~~~~~~~~~~~~~~");
        for (BoardListDto result : results) {
            System.out.println(result);
        }
        System.out.println("~~~~~~~~~~~~~~~~~~~~~");
    }

    @Test
    public void 작성글내용확인() throws Exception {
        //given
        User user = new User(1L, "찬", "fbcks97@naver.com", "picture", Role.GUEST);
        userRepository.save(user);

        for(int i=0; i<100; i++){
            Board board = new Board((long) i, "제목"+i, "내용"+i, user);
            boardRepository.save(board);
        }

        //when
        Long boardId = 3L;

        //then
        ResponseEntity<BoardPostDto> result = boardController.findBoard(boardId);
        assertThat(result.getBody().getContent()).isEqualTo("내용3");
    }

    @Test
    public void 글확인() throws Exception {
        //given
        User user = new User(1L, "찬", "fbcks97@naver.com", "picture", Role.GUEST);
        userRepository.save(user);

        for(int i=0; i<100; i++){
            Board board = new Board((long) i, "제목"+i, "내용"+i, user);
            boardRepository.save(board);
        }

        //when
        Long boardId = 3L;

        BoardPostDto boardPostDto = new BoardPostDto();
        boardPostDto.setTitle("변경 제목");
        boardPostDto.setContent("내용도 변경함");

        ResponseEntity<BoardPostDto> result = boardController.updateBoard(boardPostDto, boardId);

        entityManager.flush();
        entityManager.clear();

        Board board = boardService.findBoard(boardId);

        //then
        assertThat(result.getBody().getTitle()).isEqualTo("변경 제목");
        assertThat(result.getBody().getContent()).isEqualTo("내용도 변경함");
    }

    @Test
    public void 글삭제() throws Exception {
        //given
        User user = new User(1L, "찬", "fbcks97@naver.com", "picture", Role.GUEST);
        userRepository.save(user);

        for(int i=0; i<100; i++){
            Board board = new Board((long) i, "제목"+i, "내용"+i, user);
            boardRepository.save(board);
        }


        //when
        boardController.deleteBoard(3L);

        //then
        assertThat(boardController.findBoard(3L).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}