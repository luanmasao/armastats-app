package com.example.armastats.service;

import com.example.armastats.dto.AttendanceRecordDto;
import com.example.armastats.model.PlayerSessionStats;
import com.example.armastats.repository.PlayerSessionStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service 
@RequiredArgsConstructor
public class AttendanceService {

    private final PlayerSessionStatsRepository playerSessionStatsRepository;
    private static final double PRESENCE_THRESHOLD = 80.0;

    public List<AttendanceRecordDto> getAttendanceReport(Long sessionId) {
        List<PlayerSessionStats> statsForSession = playerSessionStatsRepository.findByGameSessionId(sessionId);

        if (statsForSession.isEmpty()) {
            return Collections.emptyList(); 
        }

        return statsForSession.stream()
                .map(this::mapToAttendanceDto) 
                .collect(Collectors.toList());
    }

    private AttendanceRecordDto mapToAttendanceDto(PlayerSessionStats stats) {
        double percentage = stats.getPresencePercentage();
        boolean present = percentage >= PRESENCE_THRESHOLD;

        return new AttendanceRecordDto(
                stats.getPlayer().getName(),
                percentage,
                present
        );
    }
}