package com.example.jmt.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@ToString(exclude = {"qFileInfos", "answers"})
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public String title;

    public String content;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "question")
    List<QFileInfo> qFileInfos = new ArrayList<>();

    @OneToMany(mappedBy = "question", orphanRemoval = true, cascade = CascadeType.REMOVE)
    List<Answer> answers = new ArrayList<>();

    @ManyToOne
    User user;
}
