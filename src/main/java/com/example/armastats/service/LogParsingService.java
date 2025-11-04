package com.example.armastats.service;

import com.example.armastats.model.GameSession;
import com.example.armastats.model.Player;
import com.example.armastats.model.PlayerSessionStats;
import com.example.armastats.repository.GameSessionRepository;
import com.example.armastats.repository.PlayerRepository;
import com.example.armastats.repository.PlayerSessionStatsRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor

public class LogParsingService {

    private final PlayerRepository playerRepository;
    private final GameSessionRepository gameSessionRepository;
    private final PlayerSessionStatsRepository playerSessionStatsRepository;

    private static final DateTimeFormatter LOG_TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm:ss");

    @Transactional
    public void parseAndSaveLog(InputStream inputStream) throws IOException {
        GameSession session = new GameSession();
        Map<String, PlayerSessionStats> playerStatsMap = new HashMap<>();
        Map<String, String> playerNameToSteamIdMap = new HashMap<>();
        
        Map<String, LocalDateTime> lastConnectTimeMap = new HashMap<>();

        boolean gameStartedFound = false;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                LocalDate today = LocalDate.now();
                LocalDateTime timestamp = parseTimestamp(line, today);

                if (timestamp != null) {
                    if (line.contains("Player") && line.contains("connected (id=")) {
                        Matcher m = Pattern.compile("Player (.*) connected \\(id=(.*)\\).").matcher(line);
                        if (m.find()) {
                            String playerName = m.group(1);
                            String steamId = m.group(2);
                            playerNameToSteamIdMap.put(playerName, steamId);

                            lastConnectTimeMap.put(playerName, timestamp);

                            playerStatsMap.computeIfAbsent(playerName, _ -> {
                                PlayerSessionStats newStats = new PlayerSessionStats();
                                newStats.setConnectTime(timestamp);
                                newStats.setAccumulatedSecondsPlayed(0L);
                                return newStats;
                            });
                        }
                    } else if (line.contains("Player") && line.contains("disconnected.")) {
                        Matcher m = Pattern.compile("Player (.*) disconnected.").matcher(line);
                        if (m.find()) {
                            String playerName = m.group(1);
                            LocalDateTime lastConnect = lastConnectTimeMap.get(playerName);
                            PlayerSessionStats stats = playerStatsMap.get(playerName);

                            if (lastConnect != null && stats != null) {
                                long secondsInThisSession = ChronoUnit.SECONDS.between(lastConnect, timestamp);
                                stats.setAccumulatedSecondsPlayed(stats.getAccumulatedSecondsPlayed() + secondsInThisSession);
                                stats.setDisconnectTime(timestamp);
                                lastConnectTimeMap.remove(playerName);
                            }
                        }
                    } else if (line.contains("Game started.")) {
                        session.setStartTime(timestamp);
                        gameStartedFound = true;
                    } else if (line.contains("Game finished.")) {
                        session.setEndTime(timestamp);

                        for (Map.Entry<String, LocalDateTime> entry : lastConnectTimeMap.entrySet()) {
                            String playerName = entry.getKey();
                            LocalDateTime lastConnect = entry.getValue();
                            PlayerSessionStats stats = playerStatsMap.get(playerName);
                            if (stats != null) {
                                long secondsInThisSession = ChronoUnit.SECONDS.between(lastConnect, session.getEndTime());
                                stats.setAccumulatedSecondsPlayed(stats.getAccumulatedSecondsPlayed() + secondsInThisSession);
                                stats.setDisconnectTime(session.getEndTime());
                            }
                        }
                        lastConnectTimeMap.clear();
                    }
                }

                if (line.contains("mission=")) {
                    session.setMissionName(extractValue(line, "mission"));
                } else if (line.contains("island=")) {
                    session.setIsland(extractValue(line, "island"));
                } else if (line.contains("gameType=")) {
                    session.setGameType(extractValue(line, "gameType"));
                } else if (line.contains("duration=")) {
                    try {
                        session.setDurationSeconds(Double.parseDouble(extractValue(line, "duration")));
                    } catch (NumberFormatException | NullPointerException e) {
                        System.err.println("WARNING: Wrong format for: 'duration' on line:" + line);
                    }
                }  else if (line.contains("class Player")) {
                    parsePlayerStatsBlock(reader, playerStatsMap);
                }
            }
        }

        if (!gameStartedFound && playerStatsMap.isEmpty()) {
        throw new IOException("The file does not appear to be a valid log or contains no session data.");
        }

        gameSessionRepository.save(session);

        for (Map.Entry<String, PlayerSessionStats> entry : playerStatsMap.entrySet()) {
            String playerName = entry.getKey();
            PlayerSessionStats stats = entry.getValue();

            String steamId = playerNameToSteamIdMap.get(playerName);
            if (steamId == null) {
                System.err.println("AVISO: Steam ID não encontrado para o jogador: " + playerName);
                continue;
            }

            Player player = playerRepository.findBySteamId(steamId)
                .map(existingPlayer -> {
                    if (!existingPlayer.getName().equals(playerName)) {
                        existingPlayer.setName(playerName);
                        return playerRepository.save(existingPlayer);
                    }
                    return existingPlayer;
                })
                .orElseGet(() -> {
                    Player newPlayer = new Player();
                    newPlayer.setSteamId(steamId);
                    newPlayer.setName(playerName);
                    return playerRepository.save(newPlayer);
                });

            stats.setPlayer(player);
            stats.setGameSession(session);
            playerSessionStatsRepository.save(stats);
        }
    }

    private LocalDateTime parseTimestamp(String line, LocalDate date) {
        Pattern p = Pattern.compile("^\\s*(\\d{1,2}:\\d{2}:\\d{2})");
        Matcher m = p.matcher(line);
        if (m.find()) {
            return LocalDateTime.of(date, LocalTime.parse(m.group(1), LOG_TIME_FORMATTER));
        }
        return null;
    }

    private String extractValue(String line, String key) {
        Pattern p = Pattern.compile(key + "\\s*=\\s*\"?(.*?)\"?;");
        Matcher m = p.matcher(line);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private void parsePlayerStatsBlock(BufferedReader reader, Map<String, PlayerSessionStats> playerStatsMap) throws IOException {
        String line;
        String currentPlayerName = null;

        while ((line = reader.readLine()) != null && !line.trim().equals("};")) {
            if (line.contains("name=")) {
                currentPlayerName = extractValue(line, "name");
            } else if (currentPlayerName != null) {
                PlayerSessionStats stats = playerStatsMap.computeIfAbsent(currentPlayerName, _ -> new PlayerSessionStats()); 
            try {
                    if (line.contains("killsInfantry=")) stats.setKillsInfantry(Integer.parseInt(extractValue(line, "killsInfantry")));
                    else if (line.contains("killsSoft=")) stats.setKillsSoft(Integer.parseInt(extractValue(line, "killsSoft")));
                    else if (line.contains("killsArmor=")) stats.setKillsArmor(Integer.parseInt(extractValue(line, "killsArmor")));
                    else if (line.contains("killsAir=")) stats.setKillsAir(Integer.parseInt(extractValue(line, "killsAir")));
                    else if (line.contains("killsPlayers=")) stats.setKillsPlayers(Integer.parseInt(extractValue(line, "killsPlayers")));
                    else if (line.contains("customScore=")) stats.setCustomScore(Integer.parseInt(extractValue(line, "customScore")));
                    else if (line.contains("killsTotal=")) stats.setKillsTotal(Integer.parseInt(extractValue(line, "killsTotal")));
                    else if (line.contains("killed=")) stats.setDeaths(Integer.parseInt(extractValue(line, "killed")));
                } catch (NumberFormatException | NullPointerException e) {
                     System.err.println("Warning: Failed to parse player data '" + currentPlayerName + "' on line: " + line);
                }
            }
        }
    }
}