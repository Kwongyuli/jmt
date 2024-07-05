package com.example.jmt.desert.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

import com.example.jmt.model.User;

@Data
public class DesertCreate {

    @NotBlank(message = "제목을 입력해주세요")
    private String title;

    @NotBlank(message = "내용을 입력해주세요")
    private String content;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    private double lat; // 위도
    private double lng; // 경도

    private User user;

    @Builder
    public DesertCreate(String title, String content, LocalDateTime createdAt, double lat, double lng, User user) {
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.lat = lat;
        this.lng = lng;
        this.user = user;
    }
    // 기본 생성자 추가
    public DesertCreate() {
    }
}
