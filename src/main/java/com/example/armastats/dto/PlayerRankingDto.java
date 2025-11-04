package com.example.armastats.dto;

public record PlayerRankingDto(
    String playerName,
    Double value
) {
    public PlayerRankingDto(String playerName, Long longValue) {
        this(playerName, longValue != null ? longValue.doubleValue() : null);
    }
}