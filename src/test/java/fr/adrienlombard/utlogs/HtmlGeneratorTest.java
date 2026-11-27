package fr.adrienlombard.utlogs;

import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HtmlGeneratorTest {

    private static Player addPlayer(Game game, String name, Team team, int... captureTimesMs) {
        Player p = game.getPlayerByName(name);
        p.setTeam(team);
        for (int t : captureTimesMs) {
            p.addFlagCapture();
            p.addFlagCaptureTime(t);
        }
        return p;
    }

    private static String generateHtml(List<Game> games) throws IOException {
        File tempOutput = File.createTempFile("html_generator_test", ".html");
        tempOutput.deleteOnExit();
        new HtmlGenerator().generate(games, tempOutput.getAbsolutePath(), "01/01/2025");
        return Files.readString(tempOutput.toPath());
    }

    private static int count(String text, String token) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(token, idx)) != -1) {
            count++;
            idx += token.length();
        }
        return count;
    }

    @Test
    public void testFlagTimeAchievementsAwardedToExtremes() {
        Game game = new Game();
        addPlayer(game, "Speedy", Team.RED, 5000, 20000);
        addPlayer(game, "Middle", Team.BLUE, 30000);
        addPlayer(game, "Turtle", Team.BLUE, 90000);
        List<Game> games = new ArrayList<>(List.of(game));

        java.util.Map<String, List<String>> achievements = new HtmlGenerator().calculateAchievements(games);

        assertTrue(achievements.get("Speedy").contains("noTimeToWaste"));
        assertFalse(achievements.get("Speedy").contains("easyGoing"));
        assertTrue(achievements.get("Turtle").contains("easyGoing"));
        assertFalse(achievements.get("Turtle").contains("noTimeToWaste"));
        assertFalse(achievements.getOrDefault("Middle", List.of()).contains("noTimeToWaste"));
        assertFalse(achievements.getOrDefault("Middle", List.of()).contains("easyGoing"));
    }

    @Test
    public void testFlagTimeAchievementsAcrossGames() {
        Game g1 = new Game();
        addPlayer(g1, "Alice", Team.RED, 40000);
        Game g2 = new Game();
        addPlayer(g2, "Bob", Team.BLUE, 3000);
        addPlayer(g2, "Alice", Team.RED, 120000);

        java.util.Map<String, List<String>> achievements = new HtmlGenerator()
                .calculateAchievements(new ArrayList<>(List.of(g1, g2)));

        assertTrue(achievements.get("Bob").contains("noTimeToWaste"));
        assertTrue(achievements.get("Alice").contains("easyGoing"));
    }

    @Test
    public void testFlagTimeAchievementsNotAwardedWithSingleCapture() {
        Game game = new Game();
        addPlayer(game, "Solo", Team.RED, 10000);

        java.util.Map<String, List<String>> achievements = new HtmlGenerator()
                .calculateAchievements(new ArrayList<>(List.of(game)));

        assertFalse(achievements.getOrDefault("Solo", List.of()).contains("noTimeToWaste"));
        assertFalse(achievements.getOrDefault("Solo", List.of()).contains("easyGoing"));
    }

    @Test
    public void testFlagTimeAchievementsNotAwardedWhenAllTimesEqual() {
        Game game = new Game();
        addPlayer(game, "A", Team.RED, 10000);
        addPlayer(game, "B", Team.BLUE, 10000);

        java.util.Map<String, List<String>> achievements = new HtmlGenerator()
                .calculateAchievements(new ArrayList<>(List.of(game)));

        assertFalse(achievements.getOrDefault("A", List.of()).contains("noTimeToWaste"));
        assertFalse(achievements.getOrDefault("B", List.of()).contains("easyGoing"));
    }

    @Test
    public void testFlagTimeAchievementsIgnoreSpectatorsAndUnknown() {
        Game game = new Game();
        addPlayer(game, "Ghost", Team.SPECTATOR, 1000);
        addPlayer(game, "Unknown", Team.RED, 500);
        addPlayer(game, "Alice", Team.RED, 20000);
        addPlayer(game, "Bob", Team.BLUE, 30000);

        java.util.Map<String, List<String>> achievements = new HtmlGenerator()
                .calculateAchievements(new ArrayList<>(List.of(game)));

        assertFalse(achievements.containsKey("Ghost"));
        assertFalse(achievements.containsKey("Unknown"));
        assertTrue(achievements.get("Alice").contains("noTimeToWaste"));
        assertTrue(achievements.get("Bob").contains("easyGoing"));
    }

    @Test
    public void testFlagTimeBadgeIconsInScoreTable() {
        Game game = new Game();
        addPlayer(game, "Speedy", Team.RED, 5000);

        java.util.Map<String, List<String>> achievements = new java.util.HashMap<>();
        achievements.put("Speedy", new ArrayList<>(List.of("noTimeToWaste", "easyGoing")));

        String html = new HtmlGenerator().generateScoreTableHTML(new ArrayList<>(List.of(game)), "t",
                new java.util.HashMap<>(), true, achievements).render();

        assertTrue(html.contains("🚀 " + Messages.get("badge.noTimeToWaste")));
        assertTrue(html.contains("🐢 " + Messages.get("badge.easyGoing")));
    }

    @Test
    public void testFlagTimeCardWithTrack() throws IOException {
        Game game = new Game();
        addPlayer(game, "Speedy", Team.RED, 5000, 12500, 20000);
        String html = generateHtml(new ArrayList<>(List.of(game)));

        assertTrue(html.contains("class=\"player-stat-card flag-time-card\""));
        assertTrue(html.contains("class=\"flag-track\""));
        assertTrue(html.contains("flag-end flag-end-fast"));
        assertTrue(html.contains("flag-end flag-end-slow"));
        // Only the intermediate capture is drawn as a dot, with its exact time
        assertEquals(1, count(html, "class=\"flag-dot\""));
        assertTrue(html.contains("title=\"12.50 s\""));
        assertTrue(html.contains("5.00 s"));
        assertTrue(html.contains("20.00 s"));
        // Summary: 3 captures, average 12.50 s
        assertTrue(html.contains(Messages.get("user.flagTimes.summary", 3, "12.50 s")));
    }

    @Test
    public void testFlagTimeDotsKeepMarginFromBounds() throws IOException {
        Game game = new Game();
        // Intermediate captures very close to the bounds
        addPlayer(game, "Speedy", Team.RED, 1000, 1001, 99999, 100000);
        String html = generateHtml(new ArrayList<>(List.of(game)));

        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("class=\"flag-dot\" style=\"left:([0-9.]+)%;\"").matcher(html);
        int found = 0;
        while (m.find()) {
            double pct = Double.parseDouble(m.group(1));
            assertTrue(pct >= 10.0 && pct <= 90.0, "Dot should stay within [10%, 90%] but was " + pct);
            found++;
        }
        assertEquals(2, found);
    }

    @Test
    public void testFlagTimeCardSingleCapture() throws IOException {
        Game game = new Game();
        addPlayer(game, "Solo", Team.RED, 10000);
        String html = generateHtml(new ArrayList<>(List.of(game)));

        assertTrue(html.contains("class=\"player-stat-card flag-time-card\""));
        assertTrue(html.contains("class=\"flag-single\""));
        assertFalse(html.contains("class=\"flag-track\""));
        assertTrue(html.contains("10.00 s"));
    }

    @Test
    public void testNoFlagTimeCardWithoutCapture() throws IOException {
        Game game = new Game();
        addPlayer(game, "Nobody", Team.RED);
        String html = generateHtml(new ArrayList<>(List.of(game)));

        assertFalse(html.contains("class=\"player-stat-card flag-time-card\""));
    }

    @Test
    public void testFlagTimeCardAggregatesAcrossGames() throws IOException {
        Game g1 = new Game();
        addPlayer(g1, "Alice", Team.RED, 8000);
        Game g2 = new Game();
        addPlayer(g2, "Alice", Team.RED, 64000);
        String html = generateHtml(new ArrayList<>(List.of(g1, g2)));

        assertTrue(html.contains("class=\"flag-track\""));
        assertTrue(html.contains("8.00 s"));
        assertTrue(html.contains("64.00 s"));
        assertTrue(html.contains(Messages.get("user.flagTimes.summary", 2, "36.00 s")));
    }

    @Test
    public void testFlagCaptureTimeBadgesAndCard() throws IOException {
        HtmlGenerator generator = new HtmlGenerator();
        List<Game> games = new ArrayList<>();
        Game game = new Game();

        Player fast = game.getPlayerByName("Speedy");
        fast.setTeam(Team.RED);
        fast.addFlagCaptureTime(5000);
        fast.addFlagCaptureTime(20000);

        Player slow = game.getPlayerByName("Turtle");
        slow.setTeam(Team.BLUE);
        slow.addFlagCaptureTime(90000);

        Player none = game.getPlayerByName("Nobody");
        none.setTeam(Team.RED);

        games.add(game);

        File tempOutput = File.createTempFile("flag_times_report", ".html");
        tempOutput.deleteOnExit();
        generator.generate(games, tempOutput.getAbsolutePath(), "01/01/2025");
        String html = Files.readString(tempOutput.toPath());

        assertTrue(html.contains("🚀 " + Messages.get("badge.noTimeToWaste")));
        assertTrue(html.contains("🐢 " + Messages.get("badge.easyGoing")));
        assertTrue(html.contains("5.00 s"));
        assertTrue(html.contains("20.00 s"));
        assertTrue(html.contains("90.00 s"));
    }

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
        p1.addHitGiven();
        p1.addHitGiven();
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

    private static Player addPlayerWithStats(Game game, String name, Team team, int assists, int drops,
            int... survivalTimes) {
        Player p = game.getPlayerByName(name);
        p.setTeam(team);
        for (int i = 0; i < assists; i++) {
            p.addAssist();
        }
        for (int i = 0; i < drops; i++) {
            p.addFlagDrop();
        }
        for (int s : survivalTimes) {
            p.addSurvivalTime(s);
        }
        return p;
    }

    @Test
    public void testBuddyAndButterfingersAwardedToMaximum() {
        Game game = new Game();
        addPlayerWithStats(game, "Helper", Team.RED, 7, 1);
        addPlayerWithStats(game, "Clumsy", Team.BLUE, 2, 5);
        addPlayerWithStats(game, "Idle", Team.BLUE, 0, 0);

        java.util.Map<String, List<String>> achievements = new HtmlGenerator()
                .calculateAchievements(new ArrayList<>(List.of(game)));

        assertTrue(achievements.get("Helper").contains("buddy"));
        assertFalse(achievements.get("Helper").contains("butterfingers"));
        assertTrue(achievements.get("Clumsy").contains("butterfingers"));
        assertFalse(achievements.get("Clumsy").contains("buddy"));
        assertFalse(achievements.getOrDefault("Idle", List.of()).contains("buddy"));
        assertFalse(achievements.getOrDefault("Idle", List.of()).contains("butterfingers"));
    }

    @Test
    public void testBuddyAndButterfingersSumAcrossGames() {
        Game g1 = new Game();
        addPlayerWithStats(g1, "Alice", Team.RED, 3, 1);
        addPlayerWithStats(g1, "Bob", Team.BLUE, 5, 2);
        Game g2 = new Game();
        addPlayerWithStats(g2, "Alice", Team.RED, 4, 3);
        addPlayerWithStats(g2, "Bob", Team.BLUE, 1, 0);

        java.util.Map<String, List<String>> achievements = new HtmlGenerator()
                .calculateAchievements(new ArrayList<>(List.of(g1, g2)));

        // Alice: 7 assists / 4 drops, Bob: 6 assists / 2 drops
        assertTrue(achievements.get("Alice").contains("buddy"));
        assertTrue(achievements.get("Alice").contains("butterfingers"));
        assertFalse(achievements.get("Bob").contains("buddy"));
    }

    @Test
    public void testBuddyAndButterfingersNotAwardedWithoutEvents() {
        Game game = new Game();
        addPlayerWithStats(game, "A", Team.RED, 0, 0);
        addPlayerWithStats(game, "B", Team.BLUE, 0, 0);

        java.util.Map<String, List<String>> achievements = new HtmlGenerator()
                .calculateAchievements(new ArrayList<>(List.of(game)));

        assertFalse(achievements.getOrDefault("A", List.of()).contains("buddy"));
        assertFalse(achievements.getOrDefault("B", List.of()).contains("butterfingers"));
    }

    @Test
    public void testBuddyAndButterfingersIgnoreSpectatorsAndUnknown() {
        Game game = new Game();
        addPlayerWithStats(game, "Ghost", Team.SPECTATOR, 50, 50);
        addPlayerWithStats(game, "Unknown", Team.RED, 40, 40);
        addPlayerWithStats(game, "Alice", Team.RED, 1, 1);

        java.util.Map<String, List<String>> achievements = new HtmlGenerator()
                .calculateAchievements(new ArrayList<>(List.of(game)));

        assertFalse(achievements.containsKey("Ghost"));
        assertFalse(achievements.containsKey("Unknown"));
        assertTrue(achievements.get("Alice").contains("buddy"));
        assertTrue(achievements.get("Alice").contains("butterfingers"));
    }

    @Test
    public void testSurvivorAwardedToLongestAverageLife() {
        Game game = new Game();
        addPlayerWithStats(game, "Tough", Team.RED, 0, 0, 40, 50, 60, 50, 50);
        addPlayerWithStats(game, "Fragile", Team.BLUE, 0, 0, 10, 20, 15, 20, 10);

        java.util.Map<String, List<String>> achievements = new HtmlGenerator()
                .calculateAchievements(new ArrayList<>(List.of(game)));

        assertTrue(achievements.get("Tough").contains("survivor"));
        assertFalse(achievements.getOrDefault("Fragile", List.of()).contains("survivor"));
    }

    @Test
    public void testSurvivorRequiresMinimumNumberOfLives() {
        Game game = new Game();
        // Great average but too few lives to be eligible
        addPlayerWithStats(game, "Lucky", Team.RED, 0, 0, 500, 600);
        addPlayerWithStats(game, "Regular", Team.BLUE, 0, 0, 20, 25, 30, 20, 25);

        java.util.Map<String, List<String>> achievements = new HtmlGenerator()
                .calculateAchievements(new ArrayList<>(List.of(game)));

        assertFalse(achievements.getOrDefault("Lucky", List.of()).contains("survivor"));
        assertTrue(achievements.get("Regular").contains("survivor"));
    }

    @Test
    public void testSurvivorLivesAreCombinedAcrossGames() {
        Game g1 = new Game();
        addPlayerWithStats(g1, "Alice", Team.RED, 0, 0, 100, 100, 100);
        addPlayerWithStats(g1, "Bob", Team.BLUE, 0, 0, 30, 30, 30);
        Game g2 = new Game();
        // Alice reaches 5 lives only thanks to the second game
        addPlayerWithStats(g2, "Alice", Team.RED, 0, 0, 100, 100);
        addPlayerWithStats(g2, "Bob", Team.BLUE, 0, 0, 30, 30);

        java.util.Map<String, List<String>> achievements = new HtmlGenerator()
                .calculateAchievements(new ArrayList<>(List.of(g1, g2)));

        assertTrue(achievements.get("Alice").contains("survivor"));
        assertFalse(achievements.getOrDefault("Bob", List.of()).contains("survivor"));
    }

    @Test
    public void testSurvivorNotAwardedWithoutSurvivalData() {
        Game game = new Game();
        addPlayerWithStats(game, "A", Team.RED, 1, 1);

        java.util.Map<String, List<String>> achievements = new HtmlGenerator()
                .calculateAchievements(new ArrayList<>(List.of(game)));

        assertFalse(achievements.getOrDefault("A", List.of()).contains("survivor"));
    }

    @Test
    public void testNewBadgeIconsAndTooltipsInScoreTable() {
        Game game = new Game();
        addPlayerWithStats(game, "Alice", Team.RED, 1, 1);

        java.util.Map<String, List<String>> achievements = new java.util.HashMap<>();
        achievements.put("Alice", new ArrayList<>(List.of("buddy", "butterfingers", "survivor")));

        String html = new HtmlGenerator().generateScoreTableHTML(new ArrayList<>(List.of(game)), "t",
                new java.util.HashMap<>(), true, achievements).render();

        assertTrue(html.contains("🤝 " + Messages.get("badge.buddy")));
        assertTrue(html.contains("🧼 " + Messages.get("badge.butterfingers")));
        assertTrue(html.contains("🏝️ " + Messages.get("badge.survivor")));
        // j2html escapes apostrophes in attribute values
        assertTrue(html.contains("title=\"" + Messages.get("badge.buddy.desc").replace("'", "&#x27;") + "\""));
        assertTrue(html.contains("title=\"" + Messages.get("badge.butterfingers.desc").replace("'", "&#x27;") + "\""));
        assertTrue(html.contains("title=\"" + Messages.get("badge.survivor.desc").replace("'", "&#x27;") + "\""));
    }

    @Test
    public void testScoreTableNewColumns() {
        Game game = new Game();
        addPlayerWithStats(game, "Alice", Team.RED, 4, 6, 20, 30, 40);
        addPlayerWithStats(game, "Bob", Team.BLUE, 0, 0);

        String html = new HtmlGenerator().generateScoreTableHTML(new ArrayList<>(List.of(game)), "t",
                new java.util.HashMap<>(), true, new java.util.HashMap<>()).render();

        assertTrue(html.contains(Messages.get("header.assists")));
        assertTrue(html.contains(Messages.get("header.flagsDropped")));
        assertTrue(html.contains(Messages.get("header.avgSurvival")));
        assertTrue(html.contains("<td>4</td>"), "Alice assists");
        assertTrue(html.contains("<td>6</td>"), "Alice flag drops");
        assertTrue(html.contains("<td>" + String.format("%.1f", 30.0) + "</td>"), "Alice average survival");
        // Bob has no complete life: no average
        assertTrue(html.contains("<td>-</td>"));
    }

    @Test
    public void testScoreTableNewColumnsAreAlsoInPerMapTable() {
        Game game = new Game();
        addPlayerWithStats(game, "Alice", Team.RED, 1, 1, 10);

        String html = new HtmlGenerator().generateScoreTableHTML(new ArrayList<>(List.of(game)), "t",
                new java.util.HashMap<>(), false, new java.util.HashMap<>()).render();

        assertTrue(html.contains(Messages.get("header.assists")));
        assertTrue(html.contains(Messages.get("header.flagsDropped")));
        assertTrue(html.contains(Messages.get("header.avgSurvival")));
        // One header cell per data cell (single player row)
        assertEquals(count(html, "<th "), count(html, "<td"));
    }

    @Test
    public void testSurvivalCard() throws IOException {
        Game game = new Game();
        addPlayerWithStats(game, "Alice", Team.RED, 0, 0, 20, 30, 40);
        String html = generateHtml(new ArrayList<>(List.of(game)));

        assertTrue(html.contains("class=\"player-stat-card survival-card\""));
        assertTrue(html.contains(Messages.get("user.survival")));
        assertTrue(html.contains("30 s"));
        assertTrue(html.contains(Messages.get("user.survival.summary", 3, "40 s")));
    }

    @Test
    public void testSurvivalCardAggregatesAcrossGames() throws IOException {
        Game g1 = new Game();
        addPlayerWithStats(g1, "Alice", Team.RED, 0, 0, 10, 20);
        Game g2 = new Game();
        addPlayerWithStats(g2, "Alice", Team.RED, 0, 0, 90);
        String html = generateHtml(new ArrayList<>(List.of(g1, g2)));

        // Average (10 + 20 + 90) / 3 = 40 s, longest life 90 s => "1 min 30 s"
        assertTrue(html.contains(Messages.get("user.survival.summary", 3, "1 min 30 s")));
        assertTrue(html.contains(">40 s<"));
    }

    @Test
    public void testNoSurvivalCardWithoutData() throws IOException {
        Game game = new Game();
        addPlayerWithStats(game, "Alice", Team.RED, 1, 1);
        String html = generateHtml(new ArrayList<>(List.of(game)));

        assertFalse(html.contains("class=\"player-stat-card survival-card\""));
    }

    @Test
    public void testSurvivalCardSitsBetweenFlagTimeAndAchievementCards() throws IOException {
        Game game = new Game();
        addPlayerWithStats(game, "Alice", Team.RED, 5, 0, 10, 20, 30, 40, 50);
        game.getPlayerByName("Alice").addFlagCaptureTime(12000);
        String html = generateHtml(new ArrayList<>(List.of(game)));

        int flagCard = html.indexOf("player-stat-card flag-time-card");
        int survivalCard = html.indexOf("player-stat-card survival-card");
        int achievementCard = html.indexOf("player-stat-card achievement-card");
        assertTrue(flagCard >= 0 && survivalCard > flagCard && achievementCard > survivalCard);
    }

    @Test
    public void testFormatSurvivalTime() {
        assertEquals("0 s", HtmlGenerator.formatSurvivalTime(0));
        assertEquals("34 s", HtmlGenerator.formatSurvivalTime(34.2));
        assertEquals("35 s", HtmlGenerator.formatSurvivalTime(34.5));
        assertEquals("1 min 00 s", HtmlGenerator.formatSurvivalTime(60));
        assertEquals("1 min 05 s", HtmlGenerator.formatSurvivalTime(65));
        assertEquals("12 min 03 s", HtmlGenerator.formatSurvivalTime(723));
    }
}
