package com.example.jmt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.jmt.model.Question;

public interface QuestionRepository extends JpaRepository<Question,Long>{
    
}
