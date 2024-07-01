package com.example.jmt.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MealCreate {

    @NotBlank(message = "제목을 입력해주세요")
    private String title;

    @NotBlank(message = "내용을 입력해주세요")
    private String content;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    private double lat; // 위도
    private double lng; // 경도

    @Builder
    public MealCreate(String title, String content, LocalDateTime createdAt, double lat, double lng) {
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.lat = lat;
        this.lng = lng;
    }
    // 기본 생성자 추가
    public MealCreate() {
    }
}
