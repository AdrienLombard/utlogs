package fr.adrienlombard.utlogs;

import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogReaderTest {

    @Test
    public void testFlagCaptureTimeAssignedToCorrectPlayerAndIgnoredAfterEnd() throws IOException {
        File tempLog = File.createTempFile("test_flag_capture_time_players", ".log");
        tempLog.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempLog)) {
            writer.write("  0:00 InitGame: \\g_gametype\\7\\mapname\\ut4_test\n");
            writer.write("  0:00 ClientUserinfoChanged: 0 n\\Player1\\t\\1\n");
            writer.write("  0:00 ClientUserinfoChanged: 1 n\\Player2\\t\\2\n");
            writer.write("  1:00 Kill: 0 1 7: Player1 killed Player2 by MOD_ROCKET\n");
            writer.write("  1:10 FlagCaptureTime: 1: 12000\n");
            writer.write("  1:20 FlagCaptureTime: 0: 3000\n");
            // Malformed line: ignored
            writer.write("  1:30 FlagCaptureTime: 0: abc\n");
            writer.write("  2:00 Exit: Timelimit hit.\n");
            // After the end of the game: ignored
            writer.write("  2:01 FlagCaptureTime: 0: 999\n");
        }

        List<Game> games = new LogReader().parse(tempLog.getAbsolutePath());

        assertEquals(1, games.size());
        assertEquals(List.of(3000), games.get(0).getPlayerById("0").getFlagCaptureTimesMs());
        assertEquals(List.of(12000), games.get(0).getPlayerById("1").getFlagCaptureTimesMs());
    }

    @Test
    public void testFlagCaptureTimesAreKeptPerGame() throws IOException {
        File tempLog = File.createTempFile("test_flag_capture_time_games", ".log");
        tempLog.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempLog)) {
            writer.write("  0:00 InitGame: \\g_gametype\\7\\mapname\\ut4_one\n");
            writer.write("  0:00 ClientUserinfoChanged: 0 n\\Player1\\t\\1\n");
            writer.write("  1:00 Kill: 0 1 7: Player1 killed Player2 by MOD_ROCKET\n");
            writer.write("  1:10 FlagCaptureTime: 0: 5000\n");
            writer.write("  2:00 Exit: Timelimit hit.\n");
            writer.write("  0:00 InitGame: \\g_gametype\\7\\mapname\\ut4_two\n");
            writer.write("  0:00 ClientUserinfoChanged: 0 n\\Player1\\t\\1\n");
            writer.write("  1:00 Kill: 0 1 7: Player1 killed Player2 by MOD_ROCKET\n");
            writer.write("  1:10 FlagCaptureTime: 0: 7000\n");
            writer.write("  2:00 Exit: Timelimit hit.\n");
        }

        List<Game> games = new LogReader().parse(tempLog.getAbsolutePath());

        assertEquals(2, games.size());
        assertEquals(List.of(5000), games.get(0).getPlayerById("0").getFlagCaptureTimesMs());
        assertEquals(List.of(7000), games.get(1).getPlayerById("0").getFlagCaptureTimesMs());
    }

    @Test
    public void testParse() throws IOException {
        // Create a temporary log file
        File tempLog = File.createTempFile("test_games", ".log");
        tempLog.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempLog)) {
            writer.write("  0:00 InitGame: \\g_gametype\\7\\mapname\\ut4_test\n");
            writer.write("  0:00 ClientUserinfoChanged: 0 n\\Player1\\t\\1\n");
            writer.write("  0:00 ClientUserinfoChanged: 1 n\\Player2\\t\\2\n");
            writer.write("  1:00 Kill: 0 1 7: Player1 killed Player2 by MOD_ROCKET\n");
            writer.write("  1:05 Kill: 1 0 7: Player2 killed Player1 by MOD_ROCKET\n");
            writer.write("  1:10 Kill: 1022 0 19: <world> killed Player1 by MOD_FALLING\n");
            // Add Hit
            writer.write("  1:15 Hit: 1 0 1 17: Player2 hit Player1 in the Kevlar\n");
            // Add Flag
            writer.write("  1:20 Flag: 0 2: Player1 captured flag\n");
            writer.write("  1:25 Flag: 1 1: Player2 returned flag\n");
            // Add Score
            writer.write("  1:30 score: 10 client: 0\n");
        }

        LogReader reader = new LogReader();
        List<Game> games = reader.parse(tempLog.getAbsolutePath());

        assertEquals(1, games.size());
        Game game = games.get(0);
        assertEquals("ut4_test", game.getMapName());
        assertEquals(3, game.getTotalKills());

        Player p1 = game.getPlayerById("0");
        assertEquals("Player1", p1.getName());
        assertEquals(Team.RED, p1.getTeam());
        assertEquals(1, p1.getKills());
        assertEquals(2, p1.getDeaths()); // 1 by Player2, 1 by World
        assertEquals(1, p1.getFlagCaptures());
        assertEquals(10, p1.getScore());
        assertEquals(1, p1.getHitsReceived());
        assertEquals(20, p1.getDamageReceived());

        Player p2 = game.getPlayerById("1");
        assertEquals("Player2", p2.getName());
        assertEquals(Team.BLUE, p2.getTeam());
        assertEquals(1, p2.getKills());
        assertEquals(1, p2.getDeaths());
        assertEquals(1, p2.getFlagReturns());
        assertEquals(1, p2.getHitsGiven());
        assertEquals(20, p2.getDamageGiven());
    }

    @Test
    public void testFallbackScores() throws IOException {
        File tempLog = File.createTempFile("test_fallback_scores", ".log");
        tempLog.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempLog)) {
            writer.write("  0:00 InitGame: \\g_gametype\\7\\mapname\\ut4_fallback\n");
            writer.write("  0:00 ClientUserinfoChanged: 0 n\\Player1\\t\\1\n");

            // Player1 gets 2 kills
            writer.write("  1:00 Kill: 0 1 7: Player1 killed Player2 by MOD_ROCKET\n");
            writer.write("  1:10 Kill: 0 1 7: Player1 killed Player2 by MOD_ROCKET\n");

            // Game ends abruptly without score lines
            writer.write("  2:00 ShutdownGame:\n");
        }

        LogReader reader = new LogReader();
        List<Game> games = reader.parse(tempLog.getAbsolutePath());

        assertEquals(1, games.size());
        Game game = games.get(0);
        Player p1 = game.getPlayerById("0");

        // Score should be equal to kills (2) because of fallback logic
        assertEquals(2, p1.getScore(), "Fallback score should equal kills");
    }

    @Test
    public void testIgnoredWeapon() throws IOException {
        File tempLog = File.createTempFile("test_ignored_weapon", ".log");
        tempLog.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempLog)) {
            writer.write("  0:00 InitGame: \\g_gametype\\7\\mapname\\ut4_ignore\n");
            writer.write("  0:00 ClientUserinfoChanged: 0 n\\Player1\\t\\1\n");
            writer.write("  0:00 ClientUserinfoChanged: 1 n\\Player2\\t\\2\n");

            // Regular kill
            writer.write("  1:00 Kill: 0 1 7: Player1 killed Player2 by MOD_ROCKET\n");

            // Kicked "kill" (should be ignored)
            writer.write("  1:05 Kill: 1022 1 24: <world> killed Player2 by UT_MOD_KICKED\n");

            writer.write("  2:00 Exit: Timelimit hit.\n");
            writer.write("  2:00 score: 1 client: 0\n");
        }

        LogReader reader = new LogReader();
        List<Game> games = reader.parse(tempLog.getAbsolutePath());

        assertEquals(1, games.size());
        Game game = games.get(0);

        // Player2 should have 1 death (from Player1), the kicked "death" should be
        // ignored
        Player p2 = game.getPlayerById("1");
        assertEquals(1, p2.getDeaths());
    }

    @Test
    public void testParseFilteredGames() throws IOException {
        File tempLog = File.createTempFile("test_filtered", ".log");
        tempLog.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempLog)) {
            // Game 1: No kills (should be filtered)
            writer.write("  0:00 InitGame: \\g_gametype\\7\\mapname\\ut4_test1\n");
            writer.write("  0:00 ClientUserinfoChanged: 0 n\\Player1\\t\\1\n");

            // Game 2: Not CTF (should be filtered)
            writer.write("  10:00 InitGame: \\g_gametype\\0\\mapname\\ut4_test2\n");
            writer.write("  10:00 ClientUserinfoChanged: 0 n\\Player1\\t\\1\n");
            writer.write("  11:00 Kill: 0 0 7: Player1 killed Player1\n");

            // Game 3: Valid CTF with kills (should be kept)
            writer.write("  20:00 InitGame: \\g_gametype\\7\\mapname\\ut4_test3\n");
            writer.write("  20:00 ClientUserinfoChanged: 0 n\\Player1\\t\\1\n");
            writer.write("  21:00 Kill: 0 0 7: Player1 killed Player1 by MOD_UNKNOWN\n");
        }

        LogReader reader = new LogReader();
        List<Game> games = reader.parse(tempLog.getAbsolutePath());

        assertEquals(1, games.size());
        assertEquals("ut4_test3", games.get(0).getMapName());
    }

    @Test
    public void testSuicidesAndWorldKills() throws IOException {
        File tempLog = File.createTempFile("reproduce_bug", ".log");
        tempLog.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempLog)) {
            writer.write("  0:00 InitGame: \\g_gametype\\7\\mapname\\ut4_test\n");
            writer.write("  0:00 ClientUserinfoChanged: 0 n\\Player1\\t\\0\n");

            // 1. Suicide by Rocket (Weapon 7) - Should be counted
            writer.write("  1:00 Kill: 0 0 7: Player1 killed Player1 by MOD_ROCKET\n");

            // 2. World Kill by Falling (Weapon 6) - Previously IGNORED, now should be
            // counted
            writer.write("  1:10 Kill: 1022 0 6: <world> killed Player1 by MOD_FALLING\n");

            // 3. World Kill by Slime (Weapon 19 - random ID) - Should be counted
            writer.write("  1:20 Kill: 1022 0 19: <world> killed Player1 by MOD_SLIME\n");

            // 4. Suicide by Falling (Weapon 6) - Previously IGNORED, now should be counted
            writer.write("  1:30 Kill: 0 0 6: Player1 killed Player1 by MOD_FALLING\n");
        }

        LogReader reader = new LogReader();
        List<Game> games = reader.parse(tempLog.getAbsolutePath());

        assertEquals(1, games.size());
        Game game = games.get(0);
        Player p1 = game.getPlayerById("0");

        // Suicides: Rocket (1) + Falling (1) = 2
        // World Kills: Falling (1) + Slime (1) = 2

        assertEquals(2, p1.getKilledByWorld(), "World Kills should be 2 (Falling + Slime)");
    }

    @Test
    public void testParseBodyHits() throws IOException {
        File tempLog = File.createTempFile("test_body_hits", ".log");
        tempLog.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempLog)) {
            writer.write("  0:00 InitGame: \\g_gametype\\7\\mapname\\ut4_test\n");
            writer.write("  0:00 ClientUserinfoChanged: 0 n\\Attacker\\t\\0\n");
            writer.write("  0:00 ClientUserinfoChanged: 1 n\\Victim\\t\\1\n");
            // Hit in Helmet
            writer.write("  1:00 Hit: 0 1 10 20: Attacker hit Victim in the Helmet\n");
            // Add a Kill to ensure game is not filtered out
            writer.write("  1:02 Kill: 0 1 7: Attacker killed Victim by MOD_ROCKET\n");
            // Hit in Kevlar
            writer.write("  1:05 Hit: 0 1 10 20: Attacker hit Victim in the Kevlar\n");
            // Hit in Torso
            writer.write("  1:10 Hit: 0 1 10 20: Attacker hit Victim in the Torso\n");
            // Hit in Body
            writer.write("  1:15 Hit: 0 1 10 20: Attacker hit Victim in the Body\n");
            // Hit in Head (simulated, though not in logs usually)
            writer.write("  1:20 Hit: 0 1 10 20: Attacker hit Victim in the Head\n");
        }

        LogReader reader = new LogReader();
        List<Game> games = reader.parse(tempLog.getAbsolutePath());

        assertEquals(1, games.size());
        Game game = games.get(0);
        Player attacker = game.getPlayerByName("Attacker");

        java.util.Map<String, Integer> bodyHits = attacker.getHitsByBodyPart();
        assertEquals(1, bodyHits.getOrDefault("Helmet", 0));
        assertEquals(1, bodyHits.getOrDefault("Kevlar", 0));
        assertEquals(1, bodyHits.getOrDefault("Torso", 0));
        assertEquals(1, bodyHits.getOrDefault("Body", 0));
        assertEquals(1, bodyHits.getOrDefault("Head", 0));
    }

    @Test
    public void testParseFlagCaptureTime() throws IOException {
        File tempLog = File.createTempFile("test_flag_capture_time", ".log");
        tempLog.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempLog)) {
            writer.write("  0:00 InitGame: \\g_gametype\\7\\mapname\\ut4_test\n");
            writer.write("  0:00 ClientUserinfoChanged: 0 n\\Player1\\t\\1\n");
            writer.write("  1:00 Kill: 0 1 7: Player1 killed Player2 by MOD_ROCKET\n");
            writer.write("  1:52 Flag: 0 2: team_CTF_redflag\n");
            writer.write("  1:52 FlagCaptureTime: 0: 17450\n");
            writer.write("  2:30 Flag: 0 2: team_CTF_redflag\n");
            writer.write("  2:30 FlagCaptureTime: 0: 8000\n");
        }

        List<Game> games = new LogReader().parse(tempLog.getAbsolutePath());

        assertEquals(1, games.size());
        Player p1 = games.get(0).getPlayerById("0");
        assertEquals(2, p1.getFlagCaptures());
        assertEquals(List.of(17450, 8000), p1.getFlagCaptureTimesMs());
    }

    @Test
    public void testParseAssists() throws IOException {
        File tempLog = File.createTempFile("test_assists", ".log");
        tempLog.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempLog)) {
            writer.write("  0:00 InitGame: \\g_gametype\\7\\mapname\\ut4_test\n");
            writer.write("  0:00 ClientUserinfoChanged: 0 n\\Player1\\t\\1\n");
            writer.write("  0:00 ClientUserinfoChanged: 1 n\\Player2\\t\\1\n");
            writer.write("  0:00 ClientUserinfoChanged: 2 n\\Player3\\t\\2\n");
            writer.write("  1:00 Kill: 1 2 7: Player2 killed Player3 by MOD_ROCKET\n");
            writer.write("  1:00 Assist: 0 1 2: Player1 assisted Player2 to kill Player3\n");
            writer.write("  1:10 Kill: 1 2 7: Player2 killed Player3 by MOD_ROCKET\n");
            writer.write("  1:10 Assist: 0 1 2: Player1 assisted Player2 to kill Player3\n");
            writer.write("  1:20 Kill: 0 2 7: Player1 killed Player3 by MOD_ROCKET\n");
            writer.write("  1:20 Assist: 1 0 2: Player2 assisted Player1 to kill Player3\n");
            writer.write("  2:00 Exit: Timelimit hit.\n");
            // After the end of the game: ignored
            writer.write("  2:01 Assist: 0 1 2: Player1 assisted Player2 to kill Player3\n");
        }

        List<Game> games = new LogReader().parse(tempLog.getAbsolutePath());

        assertEquals(1, games.size());
        assertEquals(2, games.get(0).getPlayerByName("Player1").getAssists());
        assertEquals(1, games.get(0).getPlayerByName("Player2").getAssists());
        assertEquals(0, games.get(0).getPlayerByName("Player3").getAssists());
    }

    @Test
    public void testParseFlagDrops() throws IOException {
        File tempLog = File.createTempFile("test_flag_drops", ".log");
        tempLog.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempLog)) {
            writer.write("  0:00 InitGame: \\g_gametype\\7\\mapname\\ut4_test\n");
            writer.write("  0:00 ClientUserinfoChanged: 0 n\\Player1\\t\\1\n");
            writer.write("  0:00 ClientUserinfoChanged: 1 n\\Player2\\t\\2\n");
            writer.write("  1:00 Kill: 0 1 7: Player1 killed Player2 by MOD_ROCKET\n");
            writer.write("  1:00 Flag: 1 0: team_CTF_redflag\n");
            writer.write("  1:10 Flag: 1 0: team_CTF_redflag\n");
            writer.write("  1:20 Flag: 0 0: team_CTF_blueflag\n");
            // Returns and captures must not be counted as drops
            writer.write("  1:30 Flag: 0 1: team_CTF_blueflag\n");
            writer.write("  1:40 Flag: 0 2: team_CTF_redflag\n");
            writer.write("  2:00 Exit: Timelimit hit.\n");
            // After the end of the game: ignored
            writer.write("  2:01 Flag: 1 0: team_CTF_redflag\n");
        }

        List<Game> games = new LogReader().parse(tempLog.getAbsolutePath());

        assertEquals(1, games.size());
        Player p1 = games.get(0).getPlayerById("0");
        Player p2 = games.get(0).getPlayerById("1");
        assertEquals(1, p1.getFlagDrops());
        assertEquals(1, p1.getFlagReturns());
        assertEquals(1, p1.getFlagCaptures());
        assertEquals(2, p2.getFlagDrops());
        assertEquals(0, p2.getFlagCaptures());
    }

    @Test
    public void testParseSurvivalTimes() throws IOException {
        File tempLog = File.createTempFile("test_survival", ".log");
        tempLog.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempLog)) {
            writer.write("  0:00 InitGame: \\g_gametype\\7\\mapname\\ut4_test\n");
            writer.write("  0:00 ClientUserinfoChanged: 0 n\\Player1\\t\\1\n");
            writer.write("  0:00 ClientUserinfoChanged: 1 n\\Player2\\t\\2\n");
            writer.write("  0:00 ClientUserinfoChanged: 2 n\\Player3\\t\\1\n");
            writer.write("  0:05 ClientSpawn: 1\n");
            writer.write("  0:10 ClientSpawn: 0\n");
            // Killed by a player: 35 s of life
            writer.write("  0:40 Kill: 0 1 7: Player1 killed Player2 by MOD_ROCKET\n");
            // Killed again without any new spawn: unknown life, ignored
            writer.write("  1:00 Kill: 0 1 7: Player1 killed Player2 by MOD_ROCKET\n");
            // Killed by the world: 20 s
            writer.write("  1:10 ClientSpawn: 1\n");
            writer.write("  1:30 Kill: 1022 1 19: <world> killed Player2 by MOD_SLIME\n");
            // Suicide: 5 s
            writer.write("  1:35 ClientSpawn: 1\n");
            writer.write("  1:40 Kill: 1 1 6: Player2 killed Player2 by MOD_FALLING\n");
            // Admin kick: not a real death, ignored
            writer.write("  1:50 ClientSpawn: 1\n");
            writer.write("  1:55 Kill: 1022 1 24: <world> killed Player2 by UT_MOD_KICKED\n");
            // Team kill: the victim's life ends (10 s)
            writer.write("  2:00 ClientSpawn: 2\n");
            writer.write("  2:10 Kill: 0 2 7: Player1 killed Player3 by MOD_ROCKET\n");
            // Team change: not a real death, ignored
            writer.write("  2:20 ClientSpawn: 1\n");
            writer.write("  2:25 Kill: 1 1 10: Player2 killed Player2 by MOD_CHANGE_TEAM\n");
            writer.write("  3:00 Exit: Timelimit hit.\n");
            // After the end of the game: ignored
            writer.write("  3:01 ClientSpawn: 1\n");
        }

        List<Game> games = new LogReader().parse(tempLog.getAbsolutePath());

        assertEquals(1, games.size());
        assertEquals(List.of(35, 20, 5), games.get(0).getPlayerById("1").getSurvivalTimesSeconds());
        assertEquals(List.of(10), games.get(0).getPlayerById("2").getSurvivalTimesSeconds());
        assertTrue(games.get(0).getPlayerById("0").getSurvivalTimesSeconds().isEmpty());
    }

    @Test
    public void testSurvivalTimesAreNotMixedBetweenGames() throws IOException {
        File tempLog = File.createTempFile("test_survival_games", ".log");
        tempLog.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempLog)) {
            writer.write("  0:00 InitGame: \\g_gametype\\7\\mapname\\ut4_one\n");
            writer.write("  0:00 ClientUserinfoChanged: 0 n\\Player1\\t\\1\n");
            writer.write("  0:00 ClientUserinfoChanged: 1 n\\Player2\\t\\2\n");
            writer.write("  0:10 ClientSpawn: 1\n");
            writer.write("  0:50 Kill: 0 1 7: Player1 killed Player2 by MOD_ROCKET\n");
            writer.write("  1:00 ClientSpawn: 1\n");
            writer.write("  2:00 Exit: Timelimit hit.\n");
            // Second game: the spawn of the previous game must be forgotten
            writer.write("  0:00 InitGame: \\g_gametype\\7\\mapname\\ut4_two\n");
            writer.write("  0:00 ClientUserinfoChanged: 0 n\\Player1\\t\\1\n");
            writer.write("  0:00 ClientUserinfoChanged: 1 n\\Player2\\t\\2\n");
            writer.write("  1:00 Kill: 0 1 7: Player1 killed Player2 by MOD_ROCKET\n");
            writer.write("  2:00 Exit: Timelimit hit.\n");
        }

        List<Game> games = new LogReader().parse(tempLog.getAbsolutePath());

        assertEquals(2, games.size());
        assertEquals(List.of(40), games.get(0).getPlayerById("1").getSurvivalTimesSeconds());
        assertTrue(games.get(1).getPlayerById("1").getSurvivalTimesSeconds().isEmpty());
    }
}