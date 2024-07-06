package com.example.jmt.meal.model;

import com.example.jmt.model.User;
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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public CommentMeal(Long id, String comment, LocalDateTime createdAt, Meal meal,User user) {
        this.id = id;
        this.comment = comment;
        this.createdAt = createdAt;
        this.meal = meal;
        this.user = user;
    }
}
