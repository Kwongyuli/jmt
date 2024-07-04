package com.example.jmt.desert.service;

import com.example.jmt.desert.model.CommentDesert;
import com.example.jmt.desert.model.Desert;
import com.example.jmt.desert.repository.CommentDesertRepository;
import com.example.jmt.desert.repository.DesertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentDesertService {

    private final CommentDesertRepository commentDesertRepository;
    private final DesertRepository desertRepository;

    // 댓글 추가
    public CommentDesert addComment(Long desertId, String comment) {
        Desert desert = desertRepository.findById(desertId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));
        CommentDesert commentDesert = CommentDesert.builder()
                .desert(desert)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();
        return commentDesertRepository.save(commentDesert);
    }

    // 댓글이랑 desert 엮기
    public List<CommentDesert> getCommentsByDesert(Long desertId) {
        Desert desert = desertRepository.findById(desertId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));
        return commentDesertRepository.findByDesert(desert);
    }

    // 삭제
    public void deleteComment(Long commentId) {
        commentDesertRepository.deleteById(commentId);
    }
}