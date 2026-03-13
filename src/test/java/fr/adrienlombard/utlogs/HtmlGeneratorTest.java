package fr.adrienlombard.utlogs;

import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HtmlGeneratorTest {

    @Test
    public void testGenerate() throws IOException {
        HtmlGenerator generator = new HtmlGenerator();
        List<Game> games = new ArrayList<>();
        Game game = new Game();
        game.setMapName("test_map");
        game.addKill();
        game.getPlayerByName("TestPlayer");
        games.add(game);

        File tempOutput = File.createTempFile("report", ".html");
        tempOutput.deleteOnExit();

        generator.generate(games, tempOutput.getAbsolutePath(), "01/01/2025");

        assertTrue(tempOutput.exists());
        assertTrue(tempOutput.length() > 0);

        String content = Files.readString(tempOutput.toPath());
        assertTrue(content.contains("Rapport de Match - 01/01/2025"));
        assertTrue(content.contains("TestPlayer"));
        assertTrue(content.contains("test_map"));
    }

    @Test
    public void testAssignPlayerColors() {
        HtmlGenerator generator = new HtmlGenerator();
        List<Game> games = new ArrayList<>();
        Game game = new Game();
        game.getPlayerByName("Player1");
        game.getPlayerByName("Player2");
        games.add(game);

        java.util.Map<String, String> colors = generator.assignPlayerColors(games);
        assertEquals(2, colors.size());
        assertTrue(colors.containsKey("Player1"));
        assertTrue(colors.containsKey("Player2"));
        assertTrue(colors.get("Player1").startsWith("hsl("));
    }

    @Test
    public void testGenerateScoreTableHTML() {
        HtmlGenerator generator = new HtmlGenerator();
        List<Game> games = new ArrayList<>();
        Game game = new Game();
        Player p = game.getPlayerByName("TestPlayer");
        p.setScore(100);
        p.addKill();
        games.add(game);

        java.util.Map<String, String> colors = new java.util.HashMap<>();
        colors.put("TestPlayer", "hsl(0, 0%, 50%)");

        j2html.tags.DomContent content = generator.generateScoreTableHTML(games, "testTable", colors, true,
                new java.util.HashMap<>());
        String html = content.render();

        assertTrue(html.contains("TestPlayer"));
        assertTrue(html.contains("100")); // Score
        assertTrue(html.contains("1")); // Kills
    }

    @Test
    public void testGenerateScoreTableHTML_WithPerGameStats() {
        HtmlGenerator generator = new HtmlGenerator();
        List<Game> games = new ArrayList<>();
        Game game = new Game();
        game.getPlayerByName("TestPlayer");
        games.add(game);

        java.util.Map<String, String> colors = new java.util.HashMap<>();
        j2html.tags.DomContent content = generator.generateScoreTableHTML(games, "testTable", colors, true,
                new java.util.HashMap<>());
        String html = content.render();

        // Check for columns that should BE present
        assertTrue(html.contains(Messages.get("header.gamesPlayed")));
        assertTrue(html.contains(Messages.get("header.scorePerGame")));
        assertTrue(html.contains(Messages.get("header.killsPerGame")));
        assertTrue(html.contains(Messages.get("header.flagsPerGame")));
    }

    @Test
    public void testGenerateScoreTableHTML_NoPerGameStats() {
        HtmlGenerator generator = new HtmlGenerator();
        List<Game> games = new ArrayList<>();
        Game game = new Game();
        game.getPlayerByName("TestPlayer");
        games.add(game);

        java.util.Map<String, String> colors = new java.util.HashMap<>();
        j2html.tags.DomContent content = generator.generateScoreTableHTML(games, "testTable", colors, false,
                new java.util.HashMap<>());
        String html = content.render();

        // Check for columns that should NOT be present
        assertFalse(html.contains(Messages.get("header.gamesPlayed")));
        assertFalse(html.contains(Messages.get("header.scorePerGame")));
        assertFalse(html.contains(Messages.get("header.killsPerGame")));
        assertFalse(html.contains(Messages.get("header.flagsPerGame")));
    }

    @Test
    public void testBadges() throws IOException {
        HtmlGenerator generator = new HtmlGenerator();
        List<Game> games = new ArrayList<>();
        Game game = new Game();

        // Player 1: The Brain (2 Helmet hits)
        Player p1 = game.getPlayerByName("Brainy");
        p1.addHitToBodyPart("Helmet");
        p1.addHitToBodyPart("Helmet");
        p1.setTeam(Team.RED);

        // Player 2: Quechua (2 G36 kills)
        Player p2 = game.getPlayerByName("Camper");
        p2.addKillWithWeapon("UT_MOD_G36");
        p2.addKillWithWeapon("UT_MOD_G36");
        p2.setTeam(Team.BLUE);

        // Player 3: Normal
        Player p3 = game.getPlayerByName("Normal");
        p3.setTeam(Team.RED);

        games.add(game);

        File tempOutput = File.createTempFile("badges_report", ".html");
        tempOutput.deleteOnExit();

        generator.generate(games, tempOutput.getAbsolutePath(), "01/01/2025");

        String html = Files.readString(tempOutput.toPath());

        // Verify icons
        assertTrue(html.contains("🧠"), "Should contain Brain emoji for Brainy");
        assertTrue(html.contains("Brainy"));
        assertTrue(html.contains("⛺"), "Should contain Tent emoji for Camper");
        assertTrue(html.contains("Camper"));
    }

    @Test
    public void testBadgesInTable() {
        HtmlGenerator generator = new HtmlGenerator();
        List<Game> games = new ArrayList<>();
        Game game = new Game();

        Player p1 = game.getPlayerByName("Brainy");
        p1.setTeam(Team.RED);
        p1.addHitToBodyPart("Helmet");
        p1.addHitToBodyPart("Helmet"); // Most headshots

        Player p2 = game.getPlayerByName("Camper");
        p2.setTeam(Team.BLUE);
        p2.addKillWithWeapon("UT_MOD_G36");
        p2.addKillWithWeapon("UT_MOD_G36"); // Most G36 kills

        games.add(game);

        // We need to trigger the private calculateAchievements method.
        // Since we can't calls it, we will simulate the map it produces.
        // Actually, we want to test THAT logic too.
        // But since it is private, maybe we should just test generateScoreTableHTML
        // with a manually constructed map
        // to verify the RENDER logic. The CALCULATION logic is hard to test without
        // reflection or opening visibility.
        // I'll test rendering here.

        java.util.Map<String, List<String>> achievements = new java.util.HashMap<>();
        List<String> p1Badges = new ArrayList<>();
        p1Badges.add("theBrain");
        achievements.put("Brainy", p1Badges);

        List<String> p2Badges = new ArrayList<>();
        p2Badges.add("quechua");
        achievements.put("Camper", p2Badges);

        java.util.Map<String, String> colors = new java.util.HashMap<>();

        j2html.tags.DomContent content = generator.generateScoreTableHTML(games, "testTable", colors, true,
                achievements);
        String html = content.render();

        // Verify icons
        assertTrue(html.contains("🧠"), "Should contain Brain emoji");
        assertTrue(html.contains("Brainy"));
        assertTrue(html.contains("⛺"), "Should contain Tent emoji");
        assertTrue(html.contains("Camper"));
    }
}
