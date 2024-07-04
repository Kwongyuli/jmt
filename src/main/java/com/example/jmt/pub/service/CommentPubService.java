package com.example.jmt.pub.service;

import com.example.jmt.pub.model.CommentPub;
import com.example.jmt.pub.model.Pub;
import com.example.jmt.pub.repository.CommentPubRepository;
import com.example.jmt.pub.repository.PubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentPubService {

    private final CommentPubRepository commentPubRepository;
    private final PubRepository pubRepository;

    // 댓글 추가
    public CommentPub addComment(Long pubId, String comment) {
        Pub pub = pubRepository.findById(pubId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));
        CommentPub commentPub = CommentPub.builder()
                .pub(pub)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();
        return commentPubRepository.save(commentPub);
    }

    // 댓글이랑 pub 엮기
    public List<CommentPub> getCommentsByPub(Long pubId) {
        Pub pub = pubRepository.findById(pubId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));
        return commentPubRepository.findByPub(pub);
    }

    // 삭제
    public void deleteComment(Long commentId) {
        commentPubRepository.deleteById(commentId);
    }
}