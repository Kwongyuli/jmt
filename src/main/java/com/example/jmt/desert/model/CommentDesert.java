package com.example.jmt.desert.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.example.jmt.model.User;

@Data
@Entity
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class CommentDesert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String comment;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "desert_id")
    private Desert desert;

    @ManyToOne
    User user;

    @Builder
    public CommentDesert(Long id, String comment, LocalDateTime createdAt, Desert desert, User user) {
        this.id = id;
        this.comment = comment;
        this.createdAt = createdAt;
        this.desert = desert;
        this.user = user;
    }
}
