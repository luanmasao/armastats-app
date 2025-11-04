package com.example.armastats.controller;

import com.example.armastats.service.GameSessionService;
import com.example.armastats.service.LogParsingService;
import com.example.armastats.service.PlayerService;
import com.example.armastats.dto.MissionDeathsDto;
import com.example.armastats.dto.MissionKillsDto;
import com.example.armastats.dto.MissionPlayerCountDto; 

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/admin") 
@RequiredArgsConstructor
public class AdminController {

    private final LogParsingService logParsingService;
    private final GameSessionService gameSessionService;
    private final PlayerService playerService;

    @GetMapping("/dashboard")
    public String getAdminDashboard() {
        return "admin-dashboard";
    }

@PostMapping("/upload")
public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
    if (file.isEmpty()) {
        redirectAttributes.addFlashAttribute("errorMessage", "Please, select a log to upload.");
        return "redirect:/admin/dashboard";
    }

    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".log")) {
        redirectAttributes.addFlashAttribute("errorMessage", "Invalid file type. Only .log files are allowed.");
        return "redirect:/admin/dashboard";
    }

    try {
        logParsingService.parseAndSaveLog(file.getInputStream());
        redirectAttributes.addFlashAttribute("successMessage", "Log was processed and saved.");
    } catch (Exception e) { 
        redirectAttributes.addFlashAttribute("errorMessage", "Failed to process log file: " + e.getMessage());
         e.printStackTrace(); 
    }
    return "redirect:/admin/dashboard";
}

    @PostMapping("/sessions/{sessionId}/delete")
    public String deleteSession(@PathVariable("sessionId") Long sessionId, RedirectAttributes redirectAttributes) {
        try {
            gameSessionService.deleteSessionAndAllStats(sessionId);
            redirectAttributes.addFlashAttribute("successMessage", "The mission and all of its stats were deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occured while trying to delete the mission.");
            e.printStackTrace();
        }
        return "redirect:/sessions";
    }

    @PostMapping("/players/{playerId}/delete")
    public String deletePlayer(@PathVariable("playerId") Long playerId, RedirectAttributes redirectAttributes) {
        try {
            playerService.deletePlayerAndAllStats(playerId);
            redirectAttributes.addFlashAttribute("successMessage", "The player and all of its stats were deleted with success.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An error occured while trying to delete the mission.");
            e.printStackTrace();
        }
        return "redirect:/players";
    }

    @GetMapping("/player-count-chart-data")
    @ResponseBody
    public ResponseEntity<List<MissionPlayerCountDto>> getPlayerCountChartData() {
        List<MissionPlayerCountDto> chartData = gameSessionService.getPlayerCountPerMission(15);
        
        Collections.reverse(chartData);
        
        return ResponseEntity.ok(chartData);
    }

    @GetMapping("/mission-kills-chart-data")
    @ResponseBody
    public ResponseEntity<List<MissionKillsDto>> getMissionKillsChartData() {
        List<MissionKillsDto> chartData = gameSessionService.getTotalKillsPerMission(15);
        Collections.reverse(chartData); 
        return ResponseEntity.ok(chartData);
    }

    @GetMapping("/mission-deaths-chart-data")
    @ResponseBody
    public ResponseEntity<List<MissionDeathsDto>> getMissionDeathsChartData() {
        List<MissionDeathsDto> chartData = gameSessionService.getTotalDeathsPerMission(15);
        Collections.reverse(chartData); 
        return ResponseEntity.ok(chartData);
    }
}