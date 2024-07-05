package com.example.jmt.pub.repository;

import com.example.jmt.model.Meal;
import com.example.jmt.model.User;
import com.example.jmt.pub.model.Pub;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PubRepository extends JpaRepository<Pub, Long> {

    Page<Pub> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);
    Page<Pub> findAll(Pageable pageable);

    long countByTitleContainingOrContentContaining(String search, String search1);

    List<Pub> findByUser(User user);
}
