package com.example.jmt.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.jmt.model.User;

public interface UserRepository extends JpaRepository<User, Long>{
    public User findByUserIdAndPw(String userId, String pw);
    public User findByUserId(String userId);
    public User findByName(String name);

    Optional<User> findByEmail(String email);
    Optional<User> findByNameAndEmail(String name, String email);
}
