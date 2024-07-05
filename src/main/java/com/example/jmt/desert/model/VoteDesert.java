package com.example.jmt.desert.model;

import com.example.jmt.model.User;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteDesert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "desert_id")
    private Desert desert;

    @ManyToOne
    User user;

    private boolean upvote; // true이면 추천, false이면 비추천
}