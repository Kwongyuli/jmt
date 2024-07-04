package com.example.jmt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.jmt.model.QFileInfo;
import com.example.jmt.model.Question;

public interface QFileInfoRepository extends JpaRepository<QFileInfo,Integer>{
    List<QFileInfo> findByQuestion(Question question);
}
