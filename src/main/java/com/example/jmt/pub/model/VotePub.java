package com.example.jmt.pub.model;

import com.example.jmt.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VotePub {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pub_id")
    private Pub pub;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private boolean upvote; // true이면 추천, false이면 비추천
}