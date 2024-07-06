package com.example.jmt.meal.repository;

import com.example.jmt.meal.model.CommentMeal;
import com.example.jmt.meal.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentMealRepository extends JpaRepository<CommentMeal, Long> {
    List<CommentMeal> findByMeal(Meal meal);


}