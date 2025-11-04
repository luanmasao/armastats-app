package com.example.armastats.service;

import com.example.armastats.dto.PlayerMatchesDto;
import com.example.armastats.dto.PlayerRankingDto;
import com.example.armastats.repository.PlayerSessionStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final PlayerSessionStatsRepository playerSessionStatsRepository;

    public List<PlayerRankingDto> getTotalKillsRanking() {
        return playerSessionStatsRepository.findRankingsByTotalKills();
    }

    public List<PlayerRankingDto> getKdRanking() {
        return playerSessionStatsRepository.findRankingsByKd();
    }

    public List<PlayerMatchesDto> getMatchesPlayedRanking() {
        return playerSessionStatsRepository.findRankingsByMatchesPlayed();
    }

    public List<PlayerRankingDto> getAirKillsRanking() {
        return playerSessionStatsRepository.findRankingsByAirKills();
    }

    public List<PlayerRankingDto> getPlayerKillsRanking() {
        return playerSessionStatsRepository.findRankingsByPlayerKills();
    }

    public List<PlayerRankingDto> getInfantryKillsRanking() {
        return playerSessionStatsRepository.findRankingsByInfantryKills();
    }

    public List<PlayerRankingDto> getVehicleKillsRanking() {
        return playerSessionStatsRepository.findRankingsByVehicleKills();
    }
}