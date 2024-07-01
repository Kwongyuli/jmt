package com.example.jmt.response;

import com.example.jmt.model.Meal;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MealResponse {

    private Long id;

    public String title;
    public String content;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    private double lat; // 위도
    private double lng; // 경도

    public MealResponse(Meal meal) {
        this.id = meal.getId();
        this.title = meal.getTitle();
        this.content = meal.getContent();
        this.lat = meal.getLat();
        this.lng = meal.getLng();
        this.createdAt = meal.getCreatedAt();
        this.updatedAt = meal.getUpdatedAt();
    }

    @Builder
    public MealResponse(Long id, String title, String content, LocalDateTime createdAt, LocalDateTime updatedAt, double lat, double lng) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lat = lat;
        this.lng = lng;
    }
}
