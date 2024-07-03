package com.example.jmt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.jmt.model.Answer;

public interface AnswerRepository extends JpaRepository<Answer,Long>{
    
}
