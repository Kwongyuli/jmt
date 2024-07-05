package com.example.jmt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.jmt.model.VoteQuestion;

public interface VoteQuestionRepository extends JpaRepository<VoteQuestion, Long>{
    long countByQuestionIdAndUpvote(Long questionId, boolean b);
}
