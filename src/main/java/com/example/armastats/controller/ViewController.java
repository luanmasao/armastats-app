package com.example.armastats.controller;

import com.example.armastats.dto.AttendanceRecordDto;
import com.example.armastats.model.GameSession;
import com.example.armastats.model.Player;
import com.example.armastats.model.PlayerSessionStats;
import com.example.armastats.repository.GameSessionRepository;
import com.example.armastats.repository.PlayerRepository;
import com.example.armastats.repository.PlayerSessionStatsRepository;
import com.example.armastats.service.AttendanceService;
import com.example.armastats.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final GameSessionRepository gameSessionRepository;
    private final PlayerRepository playerRepository;
    private final PlayerSessionStatsRepository playerSessionStatsRepository;
    private final AttendanceService attendanceService;
    private final RankingService rankingService;

    @GetMapping("/")
    public String getDashboard(Model model) {
        model.addAttribute("kdRanking", rankingService.getKdRanking());
        model.addAttribute("totalKillsRanking", rankingService.getTotalKillsRanking());
        model.addAttribute("matchesPlayedRanking", rankingService.getMatchesPlayedRanking());
        model.addAttribute("airKillsRanking", rankingService.getAirKillsRanking());
        model.addAttribute("playerKillsRanking", rankingService.getPlayerKillsRanking());
        model.addAttribute("infantryKillsRanking", rankingService.getInfantryKillsRanking());
        model.addAttribute("vehicleKillsRanking", rankingService.getVehicleKillsRanking());
        return "dashboard";
    }

    @GetMapping("/players")
    public String getPlayersList(Model model) {
        model.addAttribute("players", playerRepository.findAll());
        return "players-list";
    }

    @GetMapping("/players/{playerId}")
    public String getPlayerDetails(@PathVariable("playerId") Long playerId, Model model) {
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new IllegalArgumentException("Invalid player"));
        List<PlayerSessionStats> stats = playerSessionStatsRepository.findByPlayerId(playerId);

        model.addAttribute("player", player);
        model.addAttribute("stats", stats);
        return "player-details";
    }
    
    @GetMapping("/sessions")
    public String getSessionsList(Model model) {
        List<GameSession> sessions = gameSessionRepository.findAll();
        model.addAttribute("sessions", sessions);
        return "sessions-list";
    }

    @GetMapping("/sessions/{sessionId}/attendance")
    public String getSessionAttendanceReport(@PathVariable("sessionId") Long sessionId, Model model) {
        List<AttendanceRecordDto> attendanceReport = attendanceService.getAttendanceReport(sessionId);
        GameSession session = gameSessionRepository.findById(sessionId).orElse(null);

        model.addAttribute("report", attendanceReport);
        model.addAttribute("gameSession", session);
        return "session-attendance";
    }
    
    @GetMapping("/login-admin")
    public String getAdminLoginPage() {
        return "login-admin";
    }
}