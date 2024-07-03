package com.example.jmt.repository;

import com.example.jmt.model.FileInfo;
import com.example.jmt.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileInfoRepository extends JpaRepository<FileInfo, Integer> {

    List<FileInfo> findByMeal(Meal meal);

}
