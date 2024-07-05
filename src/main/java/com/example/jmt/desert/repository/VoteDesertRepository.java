package com.example.jmt.desert.repository;

import com.example.jmt.desert.model.VoteDesert;
import com.example.jmt.pub.model.VotePub;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteDesertRepository extends JpaRepository<VoteDesert, Long> {
    Long countByDesertIdAndUpvote(Long desertId, boolean upvote);
    VoteDesert findByDesertIdAndUserId(Long desertId, Long userId); // 추가

}