package com.example.armastats.repository;

import com.example.armastats.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findBySteamId(String steamId);
    Optional<Player> findByName(String name);
}