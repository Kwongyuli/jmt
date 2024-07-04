package com.example.jmt.desert.repository;

import com.example.jmt.desert.model.Desert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesertRepository extends JpaRepository<Desert, Long> {

    Page<Desert> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);
    Page<Desert> findAll(Pageable pageable);

    long countByTitleContainingOrContentContaining(String search, String search1);
}
