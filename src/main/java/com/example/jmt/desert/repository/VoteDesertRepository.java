package com.example.jmt.desert.repository;

import com.example.jmt.desert.model.VoteDesert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteDesertRepository extends JpaRepository<VoteDesert, Long> {
    Long countByDesertIdAndUpvote(Long desertId, boolean upvote);
}