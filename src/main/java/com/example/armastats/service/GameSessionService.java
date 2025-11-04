package com.example.armastats.service;

import com.example.armastats.dto.MissionDeathsDto;
import com.example.armastats.dto.MissionKillsDto;
import com.example.armastats.dto.MissionPlayerCountDto;
import com.example.armastats.repository.GameSessionRepository;
import com.example.armastats.repository.PlayerSessionStatsRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameSessionService {

    private final PlayerSessionStatsRepository playerSessionStatsRepository;
    private final GameSessionRepository gameSessionRepository;

    @Transactional
    public void deleteSessionAndAllStats(Long sessionId) {
        playerSessionStatsRepository.deleteByGameSessionId(sessionId);

        gameSessionRepository.deleteById(sessionId);
    }

    public List<MissionPlayerCountDto> getPlayerCountPerMission(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        
        return playerSessionStatsRepository.findPlayersPerMissionOrderedByDate(pageable);
    }

    public List<MissionKillsDto> getTotalKillsPerMission(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return playerSessionStatsRepository.findTotalKillsPerMission(pageable);
    }

    public List<MissionDeathsDto> getTotalDeathsPerMission(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return playerSessionStatsRepository.findTotalDeathsPerMission(pageable);
    }

}