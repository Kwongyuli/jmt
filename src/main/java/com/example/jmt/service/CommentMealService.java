package com.example.jmt.service;

import com.example.jmt.desert.model.CommentDesert;
import com.example.jmt.model.CommentMeal;
import com.example.jmt.model.Meal;
import com.example.jmt.model.User;
import com.example.jmt.repository.CommentMealRepository;
import com.example.jmt.repository.MealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentMealService {

    private final CommentMealRepository commentMealRepository;
    private final MealRepository mealRepository;

    // 댓글 추가
    public CommentMeal addComment(Long mealId, String comment, User user) {
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));
        CommentMeal commentMeal = CommentMeal.builder()
                .meal(meal)
                .user(user)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();
        return commentMealRepository.save(commentMeal);
    }

    // 댓글이랑 meal 엮기
    public List<CommentMeal> getCommentsByMeal(Long mealId) {
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));
        return commentMealRepository.findByMeal(meal);
    }

    // 댓글 ID로 댓글 가져오기
    public CommentMeal getCommentById(Long commentId) {
        return commentMealRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다. ID: " + commentId));
    }

    // 삭제
    public void deleteComment(Long commentId,User user) {
        CommentMeal commentMeal = commentMealRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        if (!commentMeal.getUser().equals(user)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        commentMealRepository.deleteById(commentId);
    }
}