package com.example.jmt.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MealUpdate {

    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt = LocalDateTime.now();
    private double lat; // 위도
    private double lng; // 경도

    @Builder
    public MealUpdate(String title, String content, LocalDateTime createdAt, LocalDateTime updatedAt, double lat, double lng) {
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lat = lat;
        this.lng = lng;
    }
}