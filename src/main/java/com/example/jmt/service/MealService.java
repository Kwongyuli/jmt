package com.example.jmt.service;

import com.example.jmt.model.Meal;
import com.example.jmt.repository.MealRepository;
import com.example.jmt.request.MealCreate;
import com.example.jmt.request.MealUpdate;
import com.example.jmt.response.MealResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MealService {

    private final MealRepository mealRepository;

//    글 저장 메서드 _ 엔티티로 넘기기
    public Meal write(MealCreate mealCreate) {

        Meal meal = Meal.builder()
                .title(mealCreate.getTitle())
                .content(mealCreate.getContent())
                .lat(mealCreate.getLat())
                .lng(mealCreate.getLng())
                .createdAt(mealCreate.getCreatedAt())
                .updatedAt(LocalDateTime.now()) // 현재 시간으로 updatedAt 설정
                .build();

        return mealRepository.save(meal);
    }

    // 단건 조회
    public MealResponse get(Long id) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

        return MealResponse.builder()
                .id(meal.getId())
                .title(meal.getTitle())
                .content(meal.getContent())
                .lat(meal.getLat())
                .lng(meal.getLng())
                .createdAt(meal.getCreatedAt())
                .updatedAt(meal.getUpdatedAt())
                .build();
    }

    // 전체 게시글 조회
    public List<MealResponse> getList() {
        return mealRepository.findAll().stream()
                .map(MealResponse::new)
                .collect(Collectors.toList());
    }

    // 글 수정
    public MealResponse update(Long id, MealUpdate mealUpdate) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

        meal.setTitle(mealUpdate.getTitle());
        meal.setContent(mealUpdate.getContent());
        meal.setLat(mealUpdate.getLat());
        meal.setLng(mealUpdate.getLng());
        meal.setCreatedAt(mealUpdate.getCreatedAt());
        meal.setUpdatedAt(LocalDateTime.now()); // 현재 시간으로 updatedAt 설정

        Meal updatedMeal = mealRepository.save(meal);

        return MealResponse.builder()
                .id(updatedMeal.getId())
                .title(updatedMeal.getTitle())
                .content(updatedMeal.getContent())
                .lat(updatedMeal.getLat())
                .lng(updatedMeal.getLng())
                .createdAt(updatedMeal.getCreatedAt())
                .build();
    }

    // 글 삭제
    public void delete(Long id) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

        mealRepository.delete(meal);
    }
}
