package com.example.jmt.repository;

import com.example.jmt.model.CommentMeal;
import com.example.jmt.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentMealRepository extends JpaRepository<CommentMeal, Long> {
    List<CommentMeal> findByMeal(Meal meal);


}