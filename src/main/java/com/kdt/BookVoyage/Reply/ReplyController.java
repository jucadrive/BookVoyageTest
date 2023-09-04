package com.kdt.BookVoyage.Reply;


import com.kdt.BookVoyage.Board.BoardDeleteDTO;
import com.kdt.BookVoyage.Board.BoardEntity;
import com.kdt.BookVoyage.Security.TokenDecoder;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class ReplyController {

    private final TokenDecoder tokenDecoder;
    private final ReplyService replyService;



    @PostMapping("/board-detail/reply-list/{id}")
    public ResponseEntity<ReplyDTO.ReplyResponseDTO> replyCreate(@PathVariable Long id, @RequestBody ReplyDTO.ReplyRequestDTO dto, HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        String accessToken = "";
        String nickname = "";
        for (Cookie cookie : cookies) {
            switch(cookie.getName()){
                case "accessToken" : accessToken = cookie.getValue();
            }
            nickname = tokenDecoder.accessTokenDecoder(accessToken, "nickname");
        }

        ReplyDTO.ReplyResponseDTO responseDTO = replyService.replyCreate(id, dto, nickname);

        return ResponseEntity.ok(responseDTO);

    }

    @GetMapping("/board-detail/reply-list/{id}")
    public ResponseEntity<List<ReplyDTO.ReplyResponseDTO>> reply_list(@PathVariable Long id) {
        List<ReplyEntity> replyList = replyService.findReplyList(id);
        List<ReplyDTO.ReplyResponseDTO> responseDTOList = replyList.stream()
                .map(reply -> {
                    ReplyDTO.ReplyResponseDTO responseDTO = new ReplyDTO.ReplyResponseDTO(reply);
                    // Null 체크 후 닉네임 설정

                    if (reply.getNickname() != null) {
                        responseDTO.setNickname(reply.getNickname());
                    } else {
                        responseDTO.setNickname("Unknown"); // 또는 원하는 다른 값으로 설정
                    }
                    return responseDTO;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOList);

    }

    @DeleteMapping("/board-detail/reply-delete/{replyId}")
    public ResponseEntity<Void> deleteReply(@PathVariable Long replyId) {
        try {
            //댓글 데이터를 DB에서 삭제
            replyService.deleteReply(replyId);
            return ResponseEntity.noContent().build(); //삭제 성공 응답
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    

}
