package fr.adrienlombard.utlogs;

import java.io.BufferedReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads and parses Unreal Tournament 4 game log files.
 * Extracts game sessions, player statistics, and events from log files.
 */
public final class LogReader {

    /**
     * Pattern to match kill events in the format "Kill: killerId victimId
     * weaponId:".
     */
    private static final Pattern KILL_PATTERN = Pattern
            .compile("Kill: (\\d+) (\\d+) (\\d+): (.*?) killed (.*) by (.*)");

    /**
     * Pattern to match hit events in the format "Hit: attackerId targetId hits
     * damage:".
     */
    private static final Pattern HIT_PATTERN = Pattern
            .compile("Hit: (\\d+) (\\d+) (\\d+) (\\d+): (.*?) hit (.*) in the (.*)");

    /** Pattern to match flag events in the format "Flag: playerId actionId:". */
    private static final Pattern FLAG_PATTERN = Pattern.compile("Flag: (\\d+) (\\d+):");

    /** Pattern to match score events in the format "score: X ... client: Y". */
    private static final Pattern SCORE_PATTERN = Pattern.compile("score: (\\d+).*client: (\\d+)");

    /** Pattern to match team scores in the format "red:X blue:Y". */
    private static final Pattern TEAM_SCORE_PATTERN = Pattern.compile("red:(\\d+)\\s+blue:(\\d+)");

    /** Pattern to match client userinfo changes. */
    private static final Pattern USERINFO_PATTERN = Pattern
            .compile("ClientUserinfoChanged: (\\d+) n\\\\(.*?)\\\\t\\\\(\\d+)");

    /** Player ID representing world/environment kills. */
    private static final String WORLD_KILL_ID = "1022";
    private static final String WORLD_KILL_NAME = "<world>";

    /** Weapon ID representing change team kills. */
    private static final String MOD_CHANGE_TEAM_ID = "10";

    /** Weapon ID representing flag explosion kills. */
    private static final String FLAG_EXPLOSION_ID = "39";

    /** Weapon name representing admin kick (not a real kill). */
    private static final String MOD_KICKED_NAME = "UT_MOD_KICKED";

    /** Key for extracting map name from InitGame line. */
    private static final String MAPNAME_KEY = "mapname";

    /** Key for extracting game type from InitGame line. */
    private static final String GAMETYPE_KEY = "g_gametype";

    /** Game type code for Capture The Flag. */
    private static final String CTF_GAMETYPE = "7";

    /** Marker for client userinfo changed events. */
    private static final String CLIENT_USERINFO_CHANGED = "ClientUserinfoChanged:";

    /** Marker for game initialization. */
    private static final String INIT_GAME = "InitGame:";

    /** Marker for game end. */
    private static final String END_GAME = "Exit:";

    /** Marker for shutdown game. */
    private static final String SHUTDOWN_GAME = "ShutdownGame:";

    /** Marker for kill events. */
    private static final String KILL = "Kill:";

    /** Marker for hit events. */
    private static final String HIT = "Hit:";

    /** Marker for flag events. */
    private static final String FLAG = "Flag:";

    /** Marker for score events. */
    private static final String SCORE = "score:";

    /** Marker for team score events. */
    private static final String TEAM_SCORE = "red:";

    /** Index of weapon id in hit pattern groups. */
    private static final int HIT_WEAPON_GROUP_INDEX = 4;

    /**
     * Parses a log file and extracts all game sessions.
     *
     * @param filePath the path to the log file to parse
     * @return a list of Game objects representing each game session
     * @throws IOException if there is an error reading the file
     */
    /** Minimum game duration in seconds to be included in the report. */
    private static final int MIN_GAME_DURATION = 30;

    private final DamageManager damageManager = DamageManager.getInstance();

    /**
     * Parses a log file and extracts all game sessions.
     *
     * @param filePath the path to the log file to parse
     * @return a list of Game objects representing each game session
     * @throws IOException if there is an error reading the file
     */
    public List<Game> parse(final String filePath) throws IOException {
        List<Game> games = new ArrayList<>();
        Game currentGame = null;
        int gameStartTime = 0;

        try (BufferedReader br = java.nio.file.Files.newBufferedReader(java.nio.file.Paths.get(filePath),
                java.nio.charset.StandardCharsets.UTF_8)) {
            String line;
            boolean endGame = false;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                int currentTime = parseTimestamp(line);

                if (line.contains(INIT_GAME)) {
                    endGame = false;
                    currentGame = new Game();
                    games.add(currentGame);
                    // Only update start time if we have a valid timestamp, otherwise default to 0
                    gameStartTime = (currentTime != -1) ? currentTime : 0;
                    parseInitGame(currentGame, line);
                } else if (currentGame != null) {
                    // Update duration continuously to handle abrupt ends, but stop if game ended
                    if (!endGame && currentTime != -1 && currentTime >= gameStartTime) {
                        currentGame.setDurationSeconds(currentTime - gameStartTime);
                    }

                    if (line.contains(CLIENT_USERINFO_CHANGED) && !endGame) {
                        parseUserInfo(currentGame, line);
                    } else if (line.contains(KILL) && !endGame) {
                        parseKill(currentGame, line);
                    } else if (line.contains(HIT) && !endGame) {
                        parseHit(currentGame, line);
                    } else if (line.contains(FLAG) && !endGame) {
                        parseFlag(currentGame, line);
                    } else if (line.contains(SCORE)) {
                        parseScore(currentGame, line);
                    } else if (line.contains(TEAM_SCORE)) {
                        parseTeamScore(currentGame, line);
                    } else if (line.contains(END_GAME) || line.contains(SHUTDOWN_GAME)) {
                        endGame = true;
                    }
                }
            }

            // Filter out games with no kills, non-CTF games, or short games
            games.removeIf(game -> game.getTotalKills() <= 0 || !isCaptureTheFlag(game)
                    || game.getDurationSeconds() < MIN_GAME_DURATION);

            // Compute fallback scores for games where score lines were missing
            // (e.g. tied games ending with ShutdownGame without Exit)
            for (Game game : games) {
                computeFallbackScores(game);
            }

        }
        return games;
    }

    /**
     * Parses the timestamp from the beginning of a log line.
     * Expected format: " M:SS ..." or " MM:SS ..."
     *
     * @param line the log line
     * @return the time in seconds, or -1 if parsing fails
     */
    private int parseTimestamp(String line) {
        try {
            int colonIndex = line.indexOf(':');
            if (colonIndex > 0 && colonIndex < 10) { // Sanity check for position
                String timePart = line.substring(0, colonIndex + 3).trim();
                String[] parts = timePart.split(":");
                if (parts.length == 2) {
                    int minutes = Integer.parseInt(parts[0].trim());
                    int seconds = Integer.parseInt(parts[1].trim());
                    return minutes * 60 + seconds;
                }
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            // Ignore parsing errors, return -1
        }
        return -1;
    }

    /**
     * Parses the InitGame line to extract map name and game type.
     *
     * @param game the current game session
     * @param line the log line containing InitGame data
     */
    private void parseInitGame(final Game game, final String line) {
        // Format: InitGame: ... mapname (backslash) ut4_company ... g_gametype
        // (backslash) 7 ...
        // Split by backslash to extract key-value pairs
        String[] parts = line.split("\\\\");
        for (int i = 0; i < parts.length - 1; i++) {
            if (MAPNAME_KEY.equals(parts[i])) {
                game.setMapName(parts[i + 1]);
            } else if (GAMETYPE_KEY.equals(parts[i])) {
                game.setGameType(parts[i + 1]);
            }
        }
    }

    /**
     * Checks if a game is Capture The Flag mode.
     *
     * @param game the game to check
     * @return true if the game is CTF, false otherwise
     */
    private boolean isCaptureTheFlag(final Game game) {
        return CTF_GAMETYPE.equals(game.getGameType());
    }

    /**
     * Parses a ClientUserinfoChanged line to extract player name and team.
     *
     * @param game the current game session
     * @param line the log line containing userinfo data
     */
    private void parseUserInfo(final Game game, final String line) {
        // ClientUserinfoChanged: 0 n\Colonel_Moutarde\t\3...
        Matcher matcher = USERINFO_PATTERN.matcher(line);
        if (matcher.find()) {
            String id = matcher.group(1);
            String name = matcher.group(2);
            String teamCode = matcher.group(3);
            game.registerPlayerId(id, name);
            Player player = game.getPlayerByName(name);
            player.setName(name);
            player.setTeam(Team.fromCode(teamCode));
        }
    }

    /**
     * Parses a Kill line to record kill and death statistics.
     *
     * @param game the current game session
     * @param line the log line containing kill data
     */
    private void parseKill(final Game game, final String line) {
        Matcher matcher = KILL_PATTERN.matcher(line);
        if (matcher.find()) {
            game.addKill();
            String killerId = matcher.group(1);
            String victimId = matcher.group(2);
            String weaponId = matcher.group(3);

            String killerName = matcher.group(4);
            String victimName = matcher.group(5);

            Player victim = game.getPlayerByName(victimName);

            boolean isWorldKill = WORLD_KILL_ID.equals(killerId) || "<world>".equals(killerName)
                    || "<non-client>".equals(killerName);

            String weaponName = matcher.group(6);

            if (MOD_KICKED_NAME.equals(weaponName)) {
                return;
            }

            if (isWorldKill) {
                victim.addKilledByWorld();
            } else if (!MOD_CHANGE_TEAM_ID.equals(weaponId) && !FLAG_EXPLOSION_ID.equals(weaponId)
                    && !MOD_KICKED_NAME.equals(weaponName)) {
                if (killerId.equals(victimId) || killerName.equals(victimName)) {
                    // Suicide
                    victim.addSuicide();
                } else {
                    // Normal kill
                    Player killer = game.getPlayerByName(killerName);

                    // Check for team kill
                    if (killer.getTeam() == victim.getTeam() && killer.getTeam() != Team.UNKNOWN
                            && killer.getTeam() != Team.FREE) {
                        killer.addTeamKill();
                        victim.addKilledByTeammates();
                    } else {
                        killer.addKill();
                        victim.addDeath();
                    }

                    // Track interactions
                    killer.addKillAgainst(victim.getName());
                    victim.addDeathBy(killer.getName());

                    // Track weapon usage
                    killer.addKillWithWeapon(weaponName);
                }
            }
        }

    }

    /**
     * Parses a Hit line to record hit and damage statistics.
     *
     * @param game the current game session
     * @param line the log line containing hit data
     */
    private void parseHit(final Game game, final String line) {
        Matcher matcher = HIT_PATTERN.matcher(line);
        if (matcher.find()) {
            String attackerName = matcher.group(5);
            String targetName = matcher.group(6);
            String bodyPart = matcher.group(7);
            String weaponId = matcher.group(HIT_WEAPON_GROUP_INDEX);
            int damage = damageManager.getDamage(weaponId, bodyPart);

            if (!attackerName.equals(targetName) && !attackerName.equals(WORLD_KILL_NAME)) {
                Player attacker = game.getPlayerByName(attackerName);

                if (bodyPart != null) {
                    attacker.addHitToBodyPart(bodyPart);
                }

                attacker.addHitGiven();
                attacker.addDamageGiven(damage);
            }
            game.getPlayerByName(targetName).addHitReceived();
            game.getPlayerByName(targetName).addDamageReceived(damage);
        }
    }

    /**
     * Extracts the body part name from a Hit log line.
     *
     * @param line the log line containing hit data
     * @return the body part name (Helmet, Kevlar, Arms, Legs) or null if not found
     */
    private String extractBodyPart(final String line) {
        // Pattern: "in the Helmet", "in the Kevlar", "in the Arms", "in the Legs"
        Pattern bodyPartPattern = Pattern.compile("in the (\\w+)");
        Matcher matcher = bodyPartPattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Parses a Flag line to record flag capture and return events.
     *
     * @param game the current game session
     * @param line the log line containing flag event data
     */
    private void parseFlag(final Game game, final String line) {
        Matcher matcher = FLAG_PATTERN.matcher(line);
        if (matcher.find()) {
            String clientId = matcher.group(1);
            int action = 0; // Initialize action to a default value
            try {
                action = Integer.parseInt(matcher.group(2));
            } catch (NumberFormatException e) {
                // Ignore malformed flag lines where action is not an integer
                return; // Skip processing this line further
            }
            Player player = game.getPlayerById(clientId);

            if (action == 1) {
                player.addFlagReturn();
            } else if (action == 2) {
                player.addFlagCapture();
            }
        }
    }

    /**
     * Parses a score line to update player scores.
     *
     * @param game the current game session
     * @param line the log line containing score data
     */
    private void parseScore(final Game game, final String line) {
        Matcher matcher = SCORE_PATTERN.matcher(line);
        if (matcher.find()) {
            int score = Integer.parseInt(matcher.group(1));
            String clientId = matcher.group(2);
            game.getPlayerById(clientId).setScore(score);
        }
    }

    /**
     * Parses a team score line to update game team scores.
     *
     * @param game the current game session
     * @param line the log line containing team score data
     */
    private void parseTeamScore(final Game game, final String line) {
        Matcher matcher = TEAM_SCORE_PATTERN.matcher(line);
        if (matcher.find()) {
            int redScore = Integer.parseInt(matcher.group(1));
            int blueScore = Integer.parseInt(matcher.group(2));
            game.setRedScore(redScore);
            game.setBlueScore(blueScore);
        }
    }

    /**
     * Computes fallback scores for players in games where score lines were missing.
     * This handles tied/interrupted games that end with ShutdownGame without Exit.
     * Only applies when ALL players have a score of 0 (indicating missing score
     * lines).
     *
     * @param game the game to compute fallback scores for
     */
    private void computeFallbackScores(final Game game) {
        // Check if all players have score 0 (indicating no score lines were present)
        boolean allZero = game.getPlayers().values().stream()
                .filter(p -> p.getTeam() != Team.SPECTATOR)
                .allMatch(p -> p.getScore() == 0);

        if (!allZero) {
            return;
        }

        // Check if any player actually participated (has kills or deaths)
        boolean hasActivity = game.getPlayers().values().stream()
                .filter(p -> p.getTeam() != Team.SPECTATOR)
                .anyMatch(p -> p.getKills() > 0 || p.getDeaths() > 0);

        if (!hasActivity) {
            return;
        }

        // Use kills as fallback score — the most visible and fair approximation
        // (real UT4 scores include assists, hits, damage, etc. that aren't decomposable
        // from logs)
        for (Player player : game.getPlayers().values()) {
            if (player.getTeam() != Team.SPECTATOR) {
                player.setScore(player.getKills());
            }
        }
    }
}
