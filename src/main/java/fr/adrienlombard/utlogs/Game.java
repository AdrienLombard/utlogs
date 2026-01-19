package fr.adrienlombard.utlogs;

import java.util.HashMap;
import java.util.Map;

public class Game {
    private Map<String, Player> players; // Key: Player Name
    private Map<String, String> idToName; // Key: Player ID, Value: Player Name
    private int totalKills;
    private String mapName;
    private String gameType;

    private int redScore;
    private int blueScore;
    private int durationSeconds;

    public Game() {
        this.players = new HashMap<>();
        this.idToName = new HashMap<>();
        this.totalKills = 0;
        this.mapName = "Unknown Map";
        this.gameType = "0"; // Default to FFA
        this.redScore = 0;
        this.blueScore = 0;
        this.durationSeconds = 0;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public int getRedScore() {
        return redScore;
    }

    public void setRedScore(int redScore) {
        this.redScore = redScore;
    }

    public int getBlueScore() {
        return blueScore;
    }

    public void setBlueScore(int blueScore) {
        this.blueScore = blueScore;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    /**
     * Registers a mapping between a player ID and a player name.
     * 
     * @param id   The player ID from the log.
     * @param name The player name.
     */
    public void registerPlayerId(String id, String name) {
        idToName.put(id, name);
    }

    /**
     * Gets or creates a player by their name.
     * 
     * @param name The player's name.
     * @return The Player object.
     */
    public Player getPlayerByName(String name) {
        return players.computeIfAbsent(name, Player::new);
    }

    /**
     * Gets a player by their ID, looking up the name first.
     * Falls back to "Unknown" if ID is not mapped.
     * 
     * @param id The player ID.
     * @return The Player object.
     */
    public Player getPlayerById(String id) {
        String name = idToName.getOrDefault(id, "Unknown");
        // If we have an ID but no name mapping yet, we might want to create a
        // placeholder or handle it.
        // For now, let's assume "Unknown" or maybe we should return a temporary player?
        // But the requirement is to use Name as identifier.
        // If name is "Unknown", we return a player named "Unknown".
        return getPlayerByName(name);
    }

    public void addKill() {
        this.totalKills++;
    }

    public Map<String, Player> getPlayers() {
        return java.util.Collections.unmodifiableMap(players);
    }

    public int getTotalKills() {
        return totalKills;
    }

    @Override
    public String toString() {
        return "Game{" +
                "mapName='" + mapName + '\'' +
                ", totalKills=" + totalKills +
                ", redScore=" + redScore +
                ", blueScore=" + blueScore +
                ", playersCount=" + players.size() +
                '}';
    }
}
