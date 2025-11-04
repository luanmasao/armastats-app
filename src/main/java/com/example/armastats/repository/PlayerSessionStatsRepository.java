package com.example.armastats.repository;

import com.example.armastats.dto.PlayerMatchesDto;
import com.example.armastats.dto.PlayerRankingDto;
import com.example.armastats.dto.MissionDeathsDto;
import com.example.armastats.dto.MissionKillsDto;
import com.example.armastats.dto.MissionPlayerCountDto;
import com.example.armastats.model.PlayerSessionStats;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerSessionStatsRepository extends JpaRepository<PlayerSessionStats, Long> {

    List<PlayerSessionStats> findByGameSessionId(Long gameSessionId);

    List<PlayerSessionStats> findByPlayerId(Long playerId);

    void deleteByGameSessionId(Long gameSessionId);

    void deleteByPlayerId(Long playerId);

    @Query("SELECT new com.example.armastats.dto.PlayerRankingDto(pss.player.name, SUM(pss.killsTotal)) " +
           "FROM PlayerSessionStats pss GROUP BY pss.player.name ORDER BY SUM(pss.killsTotal) DESC")
    List<PlayerRankingDto> findRankingsByTotalKills();

    @Query("SELECT new com.example.armastats.dto.PlayerRankingDto(pss.player.name, " +
           "CAST(SUM(pss.killsTotal) AS double) / CASE WHEN SUM(pss.deaths) = 0 THEN 1.0 ELSE CAST(SUM(pss.deaths) AS double) END) " +
           "FROM PlayerSessionStats pss GROUP BY pss.player.name ORDER BY (CAST(SUM(pss.killsTotal) AS double) / CASE WHEN SUM(pss.deaths) = 0 THEN 1.0 ELSE CAST(SUM(pss.deaths) AS double) END) DESC")
    List<PlayerRankingDto> findRankingsByKd();

    @Query("SELECT new com.example.armastats.dto.PlayerMatchesDto(pss.player.name, COUNT(pss)) " +
           "FROM PlayerSessionStats pss GROUP BY pss.player.name ORDER BY COUNT(pss) DESC")
    List<PlayerMatchesDto> findRankingsByMatchesPlayed();

        @Query("SELECT new com.example.armastats.dto.PlayerRankingDto(pss.player.name, SUM(pss.killsAir)) " +
           "FROM PlayerSessionStats pss GROUP BY pss.player.name ORDER BY SUM(pss.killsAir) DESC")
    List<PlayerRankingDto> findRankingsByAirKills();

    @Query("SELECT new com.example.armastats.dto.PlayerRankingDto(pss.player.name, SUM(pss.killsPlayers)) " +
           "FROM PlayerSessionStats pss GROUP BY pss.player.name ORDER BY SUM(pss.killsPlayers) DESC")
    List<PlayerRankingDto> findRankingsByPlayerKills();

    @Query("SELECT new com.example.armastats.dto.PlayerRankingDto(pss.player.name, SUM(pss.killsInfantry)) " +
           "FROM PlayerSessionStats pss GROUP BY pss.player.name ORDER BY SUM(pss.killsInfantry) DESC")
    List<PlayerRankingDto> findRankingsByInfantryKills();

    @Query("SELECT new com.example.armastats.dto.PlayerRankingDto(pss.player.name, SUM(pss.killsSoft + pss.killsArmor)) " +
           "FROM PlayerSessionStats pss GROUP BY pss.player.name ORDER BY SUM(pss.killsSoft + pss.killsArmor) DESC")
    List<PlayerRankingDto> findRankingsByVehicleKills();

    @Query("SELECT new com.example.armastats.dto.MissionPlayerCountDto(pss.gameSession.missionName, COUNT(DISTINCT pss.player)) " +
           "FROM PlayerSessionStats pss GROUP BY pss.gameSession, pss.gameSession.missionName ORDER BY pss.gameSession.startTime DESC")
    List<MissionPlayerCountDto> findPlayersPerMissionOrderedByDate(Pageable pageable);

    @Query("SELECT new com.example.armastats.dto.MissionKillsDto(pss.gameSession.missionName, SUM(pss.killsTotal)) " +
           "FROM PlayerSessionStats pss GROUP BY pss.gameSession, pss.gameSession.missionName ORDER BY pss.gameSession.startTime DESC")
    List<MissionKillsDto> findTotalKillsPerMission(Pageable pageable);

    @Query("SELECT new com.example.armastats.dto.MissionDeathsDto(pss.gameSession.missionName, SUM(pss.deaths)) " +
           "FROM PlayerSessionStats pss GROUP BY pss.gameSession, pss.gameSession.missionName ORDER BY pss.gameSession.startTime DESC")
    List<MissionDeathsDto> findTotalDeathsPerMission(Pageable pageable);
}