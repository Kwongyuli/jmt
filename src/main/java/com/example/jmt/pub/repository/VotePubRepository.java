package com.example.jmt.pub.repository;

import com.example.jmt.pub.model.VotePub;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotePubRepository extends JpaRepository<VotePub, Long> {
    Long countByPubIdAndUpvote(Long pubId, boolean upvote);
}