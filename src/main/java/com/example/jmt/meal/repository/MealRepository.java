package com.example.jmt.meal.repository;

import com.example.jmt.meal.model.Meal;
import com.example.jmt.model.User;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealRepository extends JpaRepository<Meal, Long> {
    Page<Meal> findByTitleContainingOrContentContaining(
            String title, String content, Pageable pageable);
            
    List<Meal> findByUser(User user);

    long countByTitleContainingOrContentContaining(String search, String search1);

}
