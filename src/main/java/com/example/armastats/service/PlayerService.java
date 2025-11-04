package com.example.armastats.service;

import com.example.armastats.repository.PlayerRepository;
import com.example.armastats.repository.PlayerSessionStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerSessionStatsRepository playerSessionStatsRepository;

    @Transactional
    public void deletePlayerAndAllStats(Long playerId) {
        playerSessionStatsRepository.deleteByPlayerId(playerId);
        
        playerRepository.deleteById(playerId);
    }
}