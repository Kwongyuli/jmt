package com.example.jmt.pub.repository;

import com.example.jmt.pub.model.CommentPub;
import com.example.jmt.pub.model.Pub;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentPubRepository extends JpaRepository<CommentPub, Long> {
    List<CommentPub> findByPub(Pub pub);


}