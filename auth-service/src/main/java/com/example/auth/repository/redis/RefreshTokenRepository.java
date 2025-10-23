package com.example.auth.repository.redis;

import com.example.auth.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    Optional<RefreshToken> findById(String tokenHash);

    List<RefreshToken> findByUserId(String userId);

    void deleteByUserId(String userId);
}