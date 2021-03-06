package com.board.board.controller;

import java.util.List;

import com.board.board.domain.Board;
import com.board.board.domain.Heart;
import com.board.board.domain.Reply;
import com.board.board.dto.Board.BoardPostDto;
import com.board.board.dto.Heart.HeartDto;
import com.board.board.dto.oauth.SessionUser;
import com.board.board.dto.reply.ReplyPostDto;
import com.board.board.dto.reply.ReplySaveDto;
import com.board.board.mapper.Board.BoardPostMapper;
import com.board.board.mapper.Heart.HeartMapper;
import com.board.board.mapper.Reply.ReplyPostMapper;
import com.board.board.mapper.Reply.ReplySaveMapper;
import com.board.board.repository.board.BoardRepository;
import com.board.board.service.BoardApiService;
import com.board.board.service.BoardService2;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
// api 표시 이후 삭제예정 -> 모두 REST로 변경 이후
@RequestMapping("/api/boards")
@RequiredArgsConstructor
class BoardApiController {
    private final HttpSession httpSession;
    private final BoardRepository boardRepository;
    private final BoardApiService boardApiService;
    private final BoardService2 boardService2;
    private final BoardPostMapper boardPostMapper;
    private final ReplySaveMapper replySaveMapper;
    private final ReplyPostMapper replyPostMapper;
    private final HeartMapper heartMapper;

    @GetMapping("/boards")
    List<BoardPostDto> all(@RequestParam(required = false, defaultValue = "") String title, @RequestParam(required = false, defaultValue = "") String content ) {
        return boardApiService.findBoardApi(title, content);
    }

    @PostMapping("/boards")
    Board newBoard(@RequestBody Board newBoard) {
        return boardRepository.save(newBoard);
    }


    @GetMapping("/boards/{id}")
    Board one(@PathVariable Long id) {
        return boardRepository.findById(id).orElse(null);
    }

    @PutMapping("/boards/{id}")
    Board replaceBoard(@RequestBody Board newBoard, @PathVariable Long id) {
        return boardRepository.findById(id)
                .map(board -> {
                    board.setTitle(newBoard.getTitle());
                    board.setContent(newBoard.getContent());
                    return boardRepository.save(board);
                })
                .orElseGet(() -> {
                    newBoard.setId(id);
                    return boardRepository.save(newBoard);
                });
    }

    @PostMapping("/post")
    Model form(Model model, @Valid BoardPostDto boardPostDto, BindingResult bindingResult, HttpSession httpSession){

        if(bindingResult.hasErrors()){      // 제목이 2글자 이하이거나 30자 이상인 경우 에러를 출력한다.
            System.out.println(bindingResult);
            model.addAttribute("result", "fail");
            model.addAttribute("change", "board/form :: info-form");
            return model;
        }

        System.out.println(boardPostDto.getContent());
        SessionUser user = (SessionUser) httpSession.getAttribute("user");
        String userEmail = user.getEmail();

        Board board = boardPostMapper.toEntity(boardPostDto);           // mapstruct를 사용하여 Dto의 정보를 entity로 바꾸어준다.
        boardPostMapper.updateFromDto(boardPostDto, board);             // null인 값들을 빼주기 위한 updateFromDto

        boardService2.save(userEmail, board);  // 글 저장 save

        model.addAttribute("result", "success");
//        return "redirect:/board/list";
        return model;
    }

//    @PreAuthorize("(isAuthenticated() and ( #userid == authentication.principal.userid )  ) or hasRole('ROLE_ADMIN')")
//    @PostAuthorize("returnObject.title == authentication.principal.username")
//    @Secured("ROLE_ADMIN") // admin사용자만 delete 메소드를 호출할 수 있도록 한다.
    @DeleteMapping("/{id}")
    void deleteBoard(@PathVariable Long id) {
        boardService2.deleteBoard(id);
    }


    // 좋아요/해제
    @PostMapping("/heart/{id}")
    public void doHeart(@PathVariable Long id){
        SessionUser user = (SessionUser) httpSession.getAttribute("user");
        long userId = user.getId();

        // 현재 로그인한 아이디와, board의 Id를 바탕으로 좋아요가 되어있나 확인한다.
//        Long wholeHeart = boardService.getHeartCount(id);
        HeartDto heartDto = heartMapper.toDto(boardService2.getMyHeart(id, userId));
        boolean myHeart = heartDto != null;     // heartDto가 있으면 true, 없으면 false

        // 좋아요가 되어 있다면 취소해줄 예정이다.
        if(myHeart){
            boardService2.deleteHeart(heartDto.getId());
        }else{
            heartDto = new HeartDto();
            heartDto.setBoardId(id);
            heartDto.setUserId(userId);
            System.out.println(heartDto);
            Heart heart = heartMapper.toEntity(heartDto);
            System.out.println(heart);
            heartMapper.updateFromDto(heartDto, heart);             // null인 값들을 빼주기 위한 updateFromDto 적용
            boardService2.saveHeart(heart);
        }
    }

    //     댓글 쓰기
    @PostMapping("/reply/{boardId}")
    public String doReply(@Valid ReplySaveDto replySaveDto, BindingResult bindingResult, HttpSession httpSession) throws Exception{

        if(bindingResult.hasErrors()) {      // 제목이 2글자 이하이거나 30자 이상인 경우 에러를 출력한다.
            StringBuilder errorMsg = new StringBuilder();

            List<ObjectError> list =  bindingResult.getAllErrors();
            for(ObjectError e : list) {
                errorMsg.append(e.getDefaultMessage());
            }
            return errorMsg.toString();
        }

        SessionUser user = (SessionUser) httpSession.getAttribute("user");
        long userId = user.getId();

        Reply reply = replySaveMapper.toEntity(replySaveDto);
        replySaveMapper.updateFromDto(replySaveDto, reply);

        if(replySaveDto.getId() > 0){       // 댓글 수정시
            if(!boardService2.confirmReply(replySaveDto.getId(), userId)){   // 본인확인 logic
                return "Nope.";
            }
        }

        boardService2.saveReply(userId, reply);  // 댓글 저장 save
        return "success";
    }

    //     댓글 보여주기
    @GetMapping("/reply/{id}")
    public List<ReplyPostDto> reply(@PathVariable Long id) throws Exception{
        SessionUser user = (SessionUser) httpSession.getAttribute("user");
        long userId = user.getId();

        List<ReplyPostDto> replyPostDto = replyPostMapper.toDtos(boardService2.outReply(id));
        for (ReplyPostDto postDto : replyPostDto) {
            postDto.setCheckUser( postDto.getUserId() == userId );
        }
        return replyPostDto;
    }

    // 댓글 삭제
    // 본래 삭제도 flag를 통해 진행하는것이 좋지만, 좋아요는 간단하기 때문에 이렇게 함.
    @DeleteMapping("/reply/{id}")
    void deleteReply(@PathVariable Long id) {
        boardService2.deleteReply(id);
    }

}