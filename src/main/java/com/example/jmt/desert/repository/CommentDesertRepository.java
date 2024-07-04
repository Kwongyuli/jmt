package com.example.jmt.desert.repository;

import com.example.jmt.desert.model.CommentDesert;
import com.example.jmt.desert.model.Desert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentDesertRepository extends JpaRepository<CommentDesert, Long> {
    List<CommentDesert> findByDesert(Desert desert);


}