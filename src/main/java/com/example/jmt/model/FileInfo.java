package com.example.jmt.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class FileInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String originalName;

    String saveName;


    // 기본 => manyToOne 작성하고 나중에
    @ManyToOne
    @JoinColumn(name = "meal_id")
    private Meal meal;

}
