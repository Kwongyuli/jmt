package com.example.jmt.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.jmt.model.Question;

public interface QuestionRepository extends JpaRepository<Question,Long>{

    Page<Question> findByTitleContainingOrContentContaining(String search, String search2, Pageable pageable);
    
}
