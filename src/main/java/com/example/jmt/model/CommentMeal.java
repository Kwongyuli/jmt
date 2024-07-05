package com.example.jmt.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class CommentMeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String comment;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "meal_id")
    private Meal meal;

    @Builder
    public CommentMeal(Long id, String comment, LocalDateTime createdAt, Meal meal) {
        this.id = id;
        this.comment = comment;
        this.createdAt = createdAt;
        this.meal = meal;
    }

    @ManyToOne
    User user;
}
