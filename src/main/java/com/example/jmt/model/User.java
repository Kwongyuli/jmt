package com.example.jmt.model;

import java.util.ArrayList;
import java.util.List;

import com.example.jmt.desert.model.CommentDesert;
import com.example.jmt.desert.model.Desert;
import com.example.jmt.desert.model.VoteDesert;
import com.example.jmt.meal.model.CommentMeal;
import com.example.jmt.meal.model.Meal;
import com.example.jmt.meal.model.Vote;
import com.example.jmt.pub.model.CommentPub;
import com.example.jmt.pub.model.Pub;
import com.example.jmt.pub.model.VotePub;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Entity
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String pw;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @OneToMany(mappedBy = "user")
    List<Answer> answers = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    List<CommentMeal> commentMeals = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    List<FileInfo> fileInfos = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    List<Meal> meals = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    List<QFileInfo> qFileInfos = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    List<Vote> votes = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    List<CommentDesert> commentDeserts = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    List<Desert> deserts = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    List<VoteDesert> voteDeserts = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    List<CommentPub> commentPubs = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    List<Pub> pubs = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    List<VotePub> votePubs = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return id != null ? id.equals(user.id) : user.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
