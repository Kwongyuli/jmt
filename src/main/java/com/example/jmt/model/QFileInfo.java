package com.example.jmt.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class QFileInfo {
    @Id
    @GeneratedValue
    Integer id;

    String originalName;
    
    String saveName;

    @ManyToOne
    Question question;

    @ManyToOne
    User user;
}

