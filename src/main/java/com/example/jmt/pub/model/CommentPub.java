package com.example.jmt.pub.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class CommentPub {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String comment;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "pub_id")
    private Pub pub;

    @Builder
    public CommentPub(Long id, String comment, LocalDateTime createdAt, Pub pub) {
        this.id = id;
        this.comment = comment;
        this.createdAt = createdAt;
        this.pub = pub;
    }
}
