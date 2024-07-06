package com.example.jmt.model;

import com.example.jmt.desert.model.Desert;
import com.example.jmt.meal.model.Meal;
import com.example.jmt.pub.model.Pub;
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

    // 기본 => manyToOne 작성하고 나중에
    @ManyToOne
    @JoinColumn(name = "desert_id")
    private Desert desert;

    // 기본 => manyToOne 작성하고 나중에
    @ManyToOne
    @JoinColumn(name = "pub_id")
    private Pub pub;

    @ManyToOne
    User user;

}
