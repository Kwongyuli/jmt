package com.example.jmt.meal.repository;

import com.example.jmt.meal.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Long countByMealIdAndUpvote(Long mealId, boolean upvote);
    Vote findByMealIdAndUserId(Long mealId, Long userId); // 추가

}