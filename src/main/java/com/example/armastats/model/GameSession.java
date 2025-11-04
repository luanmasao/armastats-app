package com.example.armastats.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class GameSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String missionName;
    private String island;
    private String gameType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double durationSeconds;
    
    public Double getDurationSeconds() {
        return this.durationSeconds;
    }
    
}