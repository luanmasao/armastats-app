package com.example.armastats.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class PlayerSessionStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;
    private LocalDateTime connectTime;
    private LocalDateTime disconnectTime;

    private Long accumulatedSecondsPlayed;

    private Integer killsInfantry;
    private Integer killsSoft;
    private Integer killsArmor;
    private Integer killsAir;
    private Integer killsPlayers;
    private Integer customScore;
    private Integer killsTotal;
    private Integer deaths;

    @Transient
    public double getPresencePercentage() {
        if (gameSession == null || gameSession.getDurationSeconds() == null || 
            gameSession.getDurationSeconds() <= 0 || accumulatedSecondsPlayed == null) {
            return 0.0;
        }

        double missionDurationSeconds = gameSession.getDurationSeconds();
        double effectiveSecondsPlayed = Math.min(accumulatedSecondsPlayed, missionDurationSeconds);

        return (effectiveSecondsPlayed / missionDurationSeconds) * 100.0;
    }
}
