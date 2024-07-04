package com.example.jmt.repository;

import com.example.jmt.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Long countByMealIdAndUpvote(Long mealId, boolean upvote);
}