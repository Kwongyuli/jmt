package com.example.jmt.controller;

import com.example.jmt.request.MealCreate;
import com.example.jmt.request.MealUpdate;
import com.example.jmt.response.MealResponse;
import com.example.jmt.service.MealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/meals")
public class MealController {

    private final MealService mealService;

    @GetMapping
    public String getMeals(Model model) {
        List<MealResponse> meals = mealService.getList();
        model.addAttribute("meals", meals);
        return "mealList";
    }

    // 글 상세페이지
    @GetMapping("/{id}")
    public String getMeal(@PathVariable Long id, Model model) {
        MealResponse meal = mealService.get(id);
        model.addAttribute("meal", meal);
        return "mealDetail";
    }

    @GetMapping("/new")
    public String createMealForm(Model model) {
        model.addAttribute("mealCreate", new MealCreate());
        return "mealForm";
    }

    // 글 작성
    @PostMapping
    public String createMeal(@Valid @ModelAttribute MealCreate mealCreate) {
        mealService.write(mealCreate);
        return "redirect:/meals";
    }


    // 글 수정 폼
    @GetMapping("/{id}/edit")
    public String editMealForm(@PathVariable Long id, Model model) {
        MealResponse meal = mealService.get(id);
        MealUpdate mealUpdate = MealUpdate.builder()
                .title(meal.getTitle())
                .content(meal.getContent())
                .createdAt(meal.getCreatedAt())
                .updatedAt(meal.getUpdatedAt()) // 수정일 가져오기
                .lat(meal.getLat())
                .lng(meal.getLng())
                .build();
        model.addAttribute("mealUpdate", mealUpdate);
        model.addAttribute("mealId", id);
        return "mealEditForm";
    }


    // 글 수정
    @PostMapping("/{id}")
    public String updateMeal(@PathVariable Long id, @Valid @ModelAttribute MealUpdate mealUpdate, BindingResult result) {
        if (result.hasErrors()) {
            return "mealEditForm";
        }
        mealService.update(id, mealUpdate);
        return "redirect:/meals";
    }
    // 글 삭제
    @PostMapping("/{id}/delete")
    public String deleteMeal(@PathVariable Long id) {
        mealService.delete(id);
        return "redirect:/meals";
    }
}