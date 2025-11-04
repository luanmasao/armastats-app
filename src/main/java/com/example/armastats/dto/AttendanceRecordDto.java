package com.example.armastats.dto;

public record AttendanceRecordDto(
    String playerName,
    double presencePercentage,
    boolean isPresent
) {
}