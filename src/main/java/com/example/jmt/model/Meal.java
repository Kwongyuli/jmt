package com.example.jmt.model;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public String title;
    public String content;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;


    private double lat; // 위도
    private double lng; // 경도

    //orphanRemoval = true, cascade = CascadeType.REMOVE -> 연관관계로 외부키 있으면 삭제를 도와준다.
    // toString 으로 stackOverFlow 발생해서 해당내역을 board 위에 어노테이션으로 exclude 시켜준다,
    @OneToMany(mappedBy = "meal",orphanRemoval = true, cascade = CascadeType.REMOVE)
    List<FileInfo> fileInfos = new ArrayList<>();

    @OneToMany(mappedBy = "meal", cascade = CascadeType.ALL)
    private List<Vote> votes;

    @OneToMany(mappedBy = "meal", cascade = CascadeType.REMOVE)
    private List<CommentMeal> commentMeals;

    @Builder
    public Meal(String title, String content, LocalDateTime createdAt, LocalDateTime updatedAt, double lat, double lng) {
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lat = lat;
        this.lng = lng;
    }
}
