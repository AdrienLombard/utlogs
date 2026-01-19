package fr.adrienlombard.utlogs;

import static j2html.TagCreator.*;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.ScriptTag;
import java.io.BufferedWriter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HtmlGenerator {
    /**
     * Assigns distinctive HSL colors to all unique players.
     * Uses golden angle to ensure colors are well-distributed.
     */
    Map<String, String> assignPlayerColors(List<Game> games) {
        Map<String, String> playerColors = new HashMap<>();
        Set<String> allPlayerNames = new HashSet<>();
        // Collect all unique player names
        for (Game game : games) {
            for (Player p : game.getPlayers().values()) {
                allPlayerNames.add(p.getName());
            }
        }
        // Sort names for consistent color assignment
        List<String> sortedNames = new ArrayList<>(allPlayerNames);
        Collections.sort(sortedNames);
        // Assign colors using golden angle for maximum distinction
        double goldenAngle = 137.508; // degrees
        for (int i = 0; i < sortedNames.size(); i++) {
            double hue = (i * goldenAngle) % 360;
            int saturation = 70 + (i % 3) * 10; // Vary saturation slightly (70-90%)
            int lightness = 50 + (i % 4) * 5; // Vary lightness slightly (50-65%)
            String color = String.format("hsl(%.0f, %d%%, %d%%)", hue, saturation, lightness);
            playerColors.put(sortedNames.get(i), color);
        }
        return playerColors;
    }

    /**
     * Generates HTML for a colored square indicator.
     * Handles both RGB and HSL color formats.
     */
    private DomContent getColorSquare(String color) {
        return span().withClass("color-square").withStyle("background-color:" + color + ";");
    }

    public void generate(List<Game> games, String outputPath, String reportDate) throws IOException {
        Map<String, String> playerColors = assignPlayerColors(games);
        String cssContent = loadResource("report.css");
        String jsContent = loadResource("report.js");
        String htmlContent = html(
                createHead(reportDate, cssContent),
                createBody(games, reportDate, playerColors, jsContent)).render();
        try (BufferedWriter writer = new BufferedWriter(
                new java.io.OutputStreamWriter(new java.io.FileOutputStream(outputPath), StandardCharsets.UTF_8))) {
            writer.write("<!DOCTYPE html>\n" + htmlContent);
        }
    }

    private ContainerTag<?> createHead(String reportDate, String cssContent) {
        return head(
                meta().withCharset("UTF-8"),
                meta().withName("viewport").withContent("width=device-width, initial-scale=1.0"),
                title(Messages.get("report.title", reportDate)),
                style(cssContent),
                script().withSrc("https://cdn.jsdelivr.net/npm/chart.js"));
    }

    private ContainerTag<?> createBody(List<Game> games, String reportDate, Map<String, String> playerColors,
            String jsContent) {
        return body(
                div(
                        h1(Messages.get("report.title", reportDate)),
                        createTabs(),
                        createScoresTab(games, playerColors),
                        createScoreMapTab(games, playerColors),
                        createUsersTab(games, playerColors)).withClass("container"),
                script(rawHtml(jsContent)),
                createChartScript(games, playerColors));
    }

    private DivTag createTabs() {
        return div(
                button(Messages.get("tab.scores")).withClass("tablinks active").attr("onclick",
                        "openTab(event, 'Scores')"),
                button(Messages.get("tab.scorePerMap")).withClass("tablinks").attr("onclick",
                        "openTab(event, 'ScoreMap')"),
                button(Messages.get("tab.users")).withClass("tablinks").attr("onclick",
                        "openTab(event, 'Users')"))
                .withClass("tab");
    }

    private DivTag createScoresTab(List<Game> games, Map<String, String> playerColors) {
        return div(
                createSummaryCards(games),
                createPodium(games),
                div(
                        div(
                                h3(Messages.get("header.killDeathRatio")),
                                div(canvas().withId("globalKDChart")).withClass("chart-container")),
                        div(
                                h3(Messages.get("header.flagsCaptured")),
                                div(canvas().withId("globalFlagsChart")).withClass("chart-container")))
                        .withClass("chart-row"),
                h2(Messages.get("section.globalScores")),
                div(generateScoreTableHTML(games, "scoresTable", playerColors, true, calculateAchievements(games)))
                        .withClass("table-container"))
                .withId("Scores")
                .withClass("tabcontent d-block");
    }

    private DivTag createSummaryCards(List<Game> games) {
        // Calculate summary stats
        int totalGames = games.size();
        int totalKills = 0;
        Map<String, Integer> playerTotalScores = new HashMap<>();

        for (Game game : games) {
            for (Player p : game.getPlayers().values()) {
                if (p.getTeam() != Team.SPECTATOR) {
                    totalKills += p.getKills();
                    playerTotalScores.put(p.getName(),
                            playerTotalScores.getOrDefault(p.getName(), 0) + (int) p.getScore());
                }
            }
        }

        // Find MVP
        String mvpPlayer = playerTotalScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        return div(
                div(
                        div(
                                div(text("🎮")).withClass("card-icon"),
                                div(
                                        div(text(Messages.get("summary.gamesPlayed"))).withClass("card-label"),
                                        div(text(String.valueOf(totalGames))).withClass("card-value")))
                                .withClass("summary-card"),
                        div(
                                div(text("⚔️")).withClass("card-icon"),
                                div(
                                        div(text(Messages.get("summary.totalKills"))).withClass("card-label"),
                                        div(text(String.valueOf(totalKills))).withClass("card-value")))
                                .withClass("summary-card"),
                        div(
                                div(text("🏆")).withClass("card-icon"),
                                div(
                                        div(text(Messages.get("summary.mvpPlayer"))).withClass("card-label"),
                                        div(text(mvpPlayer)).withClass("card-value card-value-player")))
                                .withClass("summary-card"))
                        .withClass("summary-cards-container"))
                .withClass("summary-section");
    }

    private DivTag createPodium(List<Game> games) {
        Map<String, Double> totalScores = new HashMap<>();
        for (Game game : games) {
            for (Player p : game.getPlayers().values()) {
                if (p.getTeam() != Team.SPECTATOR) {
                    totalScores.put(p.getName(), totalScores.getOrDefault(p.getName(), 0.0) + p.getScore());
                }
            }
        }

        List<Map.Entry<String, Double>> sortedScores = new ArrayList<>(totalScores.entrySet());
        sortedScores.sort(Map.Entry.<String, Double>comparingByValue().reversed());

        DivTag podiumContainer = div().withClass("podium-container");

        // Take top 3
        int count = 0;
        for (Map.Entry<String, Double> entry : sortedScores) {
            if (count >= 3)
                break;

            String playerName = entry.getKey();
            int score = entry.getValue().intValue();
            int rank = count + 1;
            String avatar = rank == 1 ? "👑" : (rank == 2 ? "🥈" : "🥉");

            podiumContainer.with(
                    div(
                            div(text(avatar)).withClass("player-avatar"),
                            div(
                                    div(text(playerName)).withClass("player-name"),
                                    div(text(String.valueOf(score))).withClass("player-score"))
                                    .withClass("player-info"),
                            div(
                                    div(text(String.valueOf(rank))).withClass("rank-label")).withClass("podium-step"))
                            .withClass("podium-item rank-" + rank));
            count++;
        }

        return div(
                podiumContainer).withClass("podium-section");
    }

    private DivTag createScoreMapTab(List<Game> games, Map<String, String> playerColors) {
        DivTag subtab = div().withClass("subtab");
        int count = 1;
        for (Game game : games) {
            String tabId = "ScoreGame" + count;
            String activeClass = (count == 1) ? " active" : "";
            subtab.with(
                    button(count + ". " + Messages.getMapName(game.getMapName()))
                            .withClass("subtablinks map-tab-link" + activeClass)
                            .attr("onclick", "openMapScore(event, '" + tabId + "')"));
            count++;
        }
        return div(
                subtab,
                each(games, game -> {
                    int i = games.indexOf(game) + 1;
                    String tabId = "ScoreGame" + i;
                    String displayClass = (i == 1) ? "d-block" : "";

                    return div(

                            div(
                                    h1(Messages.getMapName(game.getMapName())).withClass("map-name"),
                                    p(
                                            span(Messages.get("scoreMap.red") + " ")
                                                    .withClass("team-red"),
                                            span(String.valueOf(game.getRedScore())),
                                            span(" - "),
                                            span(String.valueOf(game.getBlueScore())),
                                            span(" " + Messages.get("scoreMap.blue"))
                                                    .withClass("team-blue"))
                                            .withClass("score-display"),

                                    h2(text(Messages.get("report.totalKills",
                                            game.getTotalKills()))).withClass("total-kills"),
                                    div(generateScoreTableHTML(Collections.singletonList(game),
                                            "table_" + tabId, playerColors, false, new HashMap<>()))
                                            .withClass("table-container"))
                                    .withId(tabId).withClass("subtabcontent map-tab-content " + displayClass));
                })).withId("ScoreMap").withClass("tabcontent");
    }

    private DivTag createUsersTab(List<Game> games, Map<String, String> playerColors) {
        Set<String> allPlayerNamesSet = new HashSet<>();
        for (Game game : games) {
            for (Player p : game.getPlayers().values()) {
                allPlayerNamesSet.add(p.getName());
            }
        }
        List<String> allPlayerNames = new ArrayList<>(allPlayerNamesSet);
        Collections.sort(allPlayerNames);

        // Calculate achievements once for all players
        Map<String, List<String>> achievements = calculateAchievements(games);

        DivTag subtab = div().withClass("subtab");
        int count = 1;
        for (String playerName : allPlayerNames) {
            String tabId = "User" + count;
            String activeClass = (count == 1) ? " active" : "";
            subtab.with(
                    button(playerName)
                            .withClass("subtablinks user-tab-link" + activeClass)
                            .attr("onclick", "openUser(event, '" + tabId + "')"));
            count++;
        }
        return div(
                subtab,
                each(allPlayerNames, playerName -> {
                    int i = allPlayerNames.indexOf(playerName) + 1;
                    String tabId = "User" + i;
                    String displayClass = (i == 1) ? "d-block" : "";

                    // Calculate player stats
                    int totalScore = 0;
                    int totalKills = 0;
                    int totalDeaths = 0;
                    int gamesPlayed = 0;
                    List<Integer> scoresPerGame = new ArrayList<>();
                    List<Integer> killsPerGame = new ArrayList<>();
                    List<Integer> deathsPerGame = new ArrayList<>();

                    // Aggregate interactions
                    Map<String, Integer> killsAgainst = new HashMap<>();
                    Map<String, Integer> deathsBy = new HashMap<>();
                    Map<String, Integer> weapons = new HashMap<>();
                    Map<String, Integer> bodyPartHits = new HashMap<>();

                    for (Game game : games) {
                        for (Player p : game.getPlayers().values()) {
                            if (p.getName().equals(playerName)) {
                                if (p.getTeam() != Team.SPECTATOR) {
                                    totalScore += (int) p.getScore();
                                    totalKills += p.getKills();
                                    totalDeaths += p.getDeaths();
                                    gamesPlayed++;
                                    scoresPerGame.add((int) p.getScore());
                                    killsPerGame.add(p.getKills());
                                    deathsPerGame.add(p.getDeaths());
                                }
                                p.getKillsByVictim().forEach((k, v) -> killsAgainst
                                        .merge(k, v, Integer::sum));
                                p.getDeathsByKiller().forEach((k, v) -> deathsBy
                                        .merge(k, v, Integer::sum));
                                p.getKillsByWeapon().forEach((k, v) -> weapons
                                        .merge(k, v, Integer::sum));
                                p.getHitsByBodyPart().forEach((k, v) -> bodyPartHits
                                        .merge(k, v, Integer::sum));
                            }
                        }
                    }

                    double kdRatio = totalDeaths > 0 ? (double) totalKills / totalDeaths : totalKills;
                    List<String> playerBadges = achievements.getOrDefault(playerName, Collections.emptyList());

                    // Find favorite victim and nemesis
                    String favoriteVictim = killsAgainst.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey).orElse(Messages.get("user.na"));
                    int favoriteVictimKills = killsAgainst.getOrDefault(favoriteVictim, 0);

                    String nemesis = deathsBy.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey).orElse(Messages.get("user.na"));
                    int nemesisKills = deathsBy.getOrDefault(nemesis, 0);

                    return div(
                            h2(playerName),
                            // Player stats cards (including victim/nemesis)
                            createPlayerStatsCards(totalScore, kdRatio, gamesPlayed, playerBadges,
                                    favoriteVictim, favoriteVictimKills, nemesis, nemesisKills),
                            // Performance timeline charts (score and K/D)
                            createPerformanceTimelineSection(tabId, scoresPerGame, killsPerGame, deathsPerGame),
                            // Body hit diagram section
                            h3(Messages.get("bodyHit.title")),
                            createBodyDiagram(bodyPartHits),
                            // Kills distribution chart and table
                            h3(Messages.get("user.killDistribution")),
                            div(
                                    div(
                                            canvas().withId("chart_"
                                                    + tabId))
                                            .withClass("chart-container"),
                                    div(
                                            createUserTable(killsAgainst,
                                                    deathsBy,
                                                    "table_" + tabId,
                                                    playerColors))
                                            .withClass("table-container"))
                                    .withClass("chart-row"),
                            // Weapons section
                            h3(Messages.get("user.favoriteWeapons")),
                            div(
                                    div(
                                            canvas().withId("weaponChart_"
                                                    + tabId))
                                            .withClass("chart-container"),
                                    div(
                                            createWeaponTable(weapons,
                                                    "weaponTable_" + tabId))
                                            .withClass("weapon-table-container"))
                                    .withClass("chart-row"))
                            .withId(tabId).withClass("subtabcontent user-tab-content " + displayClass);
                })).withId("Users").withClass("tabcontent");
    }

    private DivTag createPlayerStatsCards(int totalScore, double kdRatio, int gamesPlayed, List<String> badges,
            String favoriteVictim, int favoriteVictimKills, String nemesis, int nemesisKills) {
        DivTag statsContainer = div().withClass("player-stats-container");

        // Add first 3 cards
        statsContainer.with(
                div(
                        div(text("📊")).withClass("card-icon"),
                        div(
                                div(text(Messages.get("user.stats.totalScore"))).withClass("card-label"),
                                div(text(String.valueOf(totalScore))).withClass("card-value")))
                        .withClass("player-stat-card"),
                div(
                        div(text("⚔️")).withClass("card-icon"),
                        div(
                                div(text(Messages.get("user.stats.kdRatio"))).withClass("card-label"),
                                div(text(String.format("%.2f", kdRatio))).withClass("card-value")))
                        .withClass("player-stat-card"),
                div(
                        div(text("🎮")).withClass("card-icon"),
                        div(
                                div(text(Messages.get("user.stats.gamesPlayed"))).withClass("card-label"),
                                div(text(String.valueOf(gamesPlayed))).withClass("card-value")))
                        .withClass("player-stat-card"),
                // Favorite victim card
                div(
                        div(text("🎯")).withClass("card-icon"),
                        div(
                                div(text(Messages.get("user.favoriteVictim"))).withClass("card-label"),
                                div(text(favoriteVictim + " (" + favoriteVictimKills + ")"))
                                        .withClass("card-value-small")))
                        .withClass("player-stat-card"),
                // Nemesis card
                div(
                        div(text("☠️")).withClass("card-icon"),
                        div(
                                div(text(Messages.get("user.nemesis"))).withClass("card-label"),
                                div(text(nemesis + " (" + nemesisKills + ")")).withClass("card-value-small")))
                        .withClass("player-stat-card"));

        // Only add achievement card if player has badges
        if (!badges.isEmpty()) {
            statsContainer.with(
                    div(
                            div(text("🏆")).withClass("card-icon"),
                            div(
                                    div(text(Messages.get("user.stats.achievements"))).withClass("card-label"),
                                    div(
                                            each(badges, badgeKey -> {
                                                String icon = "";
                                                switch (badgeKey) {
                                                    case "topKiller":
                                                        icon = "👑";
                                                        break;
                                                    case "sharpshooter":
                                                        icon = "🎯";
                                                        break;
                                                    case "champion":
                                                        icon = "🏆";
                                                        break;
                                                    case "grimReaper":
                                                        icon = "💀";
                                                        break;
                                                    case "tank":
                                                        icon = "🛡️";
                                                        break;
                                                    case "theBrain":
                                                        icon = "🧠";
                                                        break;
                                                    case "quechua":
                                                        icon = "⛺";
                                                        break;
                                                    case "lemming":
                                                        icon = "🐹";
                                                        break;
                                                    case "bomberman":
                                                        icon = "💣";
                                                        break;
                                                }
                                                String badgeName = Messages.get("badge." + badgeKey);
                                                String badgeDesc = Messages.get("badge." + badgeKey + ".desc");
                                                return span(text(icon + " " + badgeName + " "))
                                                        .withClass("achievement-badge")
                                                        .withTitle(badgeDesc);
                                            })).withClass("achievement-showcase")))
                            .withClass("player-stat-card achievement-card"));
        }

        return div(statsContainer).withClass("player-stats-section");
    }

    private DivTag createPerformanceTimelineSection(String tabId, List<Integer> scoresPerGame,
            List<Integer> killsPerGame, List<Integer> deathsPerGame) {
        if (scoresPerGame.isEmpty()) {
            return div(); // Empty if no data
        }
        return div(
                h3(Messages.get("user.performanceTimeline")),
                div(
                        div(
                                h4(Messages.get("user.scoreProgression")),
                                div(
                                        canvas().withId("timelineChart_" + tabId))
                                        .withClass("chart-container timeline-chart"))
                                .withClass("timeline-chart-item"),
                        div(
                                h4(Messages.get("user.kdProgression")),
                                div(
                                        canvas().withId("kdTimelineChart_" + tabId))
                                        .withClass("chart-container timeline-chart"))
                                .withClass("timeline-chart-item"))
                        .withClass("timeline-charts-row"))
                .withClass("timeline-section");
    }

    private ContainerTag<?> createUserTable(Map<String, Integer> killsAgainst, Map<String, Integer> deathsBy,
            String tableId, Map<String, String> playerColors) {
        Set<String> opponents = new HashSet<>();
        opponents.addAll(killsAgainst.keySet());
        opponents.addAll(deathsBy.keySet());
        List<String> sortedOpponents = new ArrayList<>(opponents);
        Collections.sort(sortedOpponents);
        return table(
                thead(
                        tr(
                                thHeader(Messages.get("header.player"), 0, tableId),
                                thHeader(Messages.get("header.killsOn"), 1, tableId),
                                thHeader(Messages.get("header.deathsBy"), 2, tableId))),
                tbody(
                        each(sortedOpponents, opponent -> {
                            String opponentColor = playerColors.getOrDefault(opponent,
                                    "hsl(0, 0%, 50%)");
                            return tr(
                                    td(getColorSquare(opponentColor),
                                            text(opponent)),
                                    td(String.valueOf(killsAgainst
                                            .getOrDefault(opponent, 0))),
                                    td(String.valueOf(deathsBy
                                            .getOrDefault(opponent, 0))));
                        })))
                .withId(tableId);
    }

    private ContainerTag<?> createWeaponTable(Map<String, Integer> weapons, String tableId) {
        List<Map.Entry<String, Integer>> sortedWeapons = new ArrayList<>(weapons.entrySet());
        sortedWeapons.sort(Map.Entry.<String, Integer>comparingByValue().reversed());

        return table(
                thead(
                        tr(
                                th(Messages.get("user.weapon")),
                                th(Messages.get("user.kills")))),
                tbody(
                        each(sortedWeapons, entry -> {
                            return tr(
                                    td(text(Messages.getWeaponName(entry.getKey()))),
                                    td(String.valueOf(entry.getValue())));
                        })))
                .withId(tableId);
    }

    private Map<String, List<String>> calculateAchievements(List<Game> games) {
        Map<String, List<String>> playerAchievements = new HashMap<>();
        Map<String, Integer> totalKills = new HashMap<>();
        Map<String, Integer> totalDeaths = new HashMap<>();
        Map<String, Integer> totalFlags = new HashMap<>();
        Map<String, Integer> maxKillsInGame = new HashMap<>();
        Map<String, Integer> headshotHits = new HashMap<>();

        // Aggregate stats
        for (Game game : games) {
            for (Player p : game.getPlayers().values()) {
                if (p.getTeam() == Team.SPECTATOR)
                    continue;

                String name = p.getName();
                totalKills.merge(name, p.getKills(), Integer::sum);
                totalDeaths.merge(name, p.getDeaths(), Integer::sum);
                totalFlags.merge(name, p.getFlagCaptures(), Integer::sum);

                int currentMax = maxKillsInGame.getOrDefault(name, 0);
                if (p.getKills() > currentMax) {
                    maxKillsInGame.put(name, p.getKills());
                }

                headshotHits.merge(name, p.getHitsByBodyPart().getOrDefault("Helmet", 0), Integer::sum);
            }
        }

        // Find top values
        String topKiller = totalKills.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(null);

        String mostFlags = totalFlags.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(null);

        String grimReaper = maxKillsInGame.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(null);

        String tank = totalDeaths.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(null);

        String theBrain = headshotHits.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(null);

        // Max suicides (Lemming)
        String lemming = games.stream()
                .flatMap(g -> g.getPlayers().values().stream())
                .filter(p -> p.getTeam() != Team.SPECTATOR)
                .collect(java.util.stream.Collectors.groupingBy(Player::getName,
                        java.util.stream.Collectors.summingInt(Player::getSuicides)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .filter(e -> e.getValue() > 0)
                .map(Map.Entry::getKey).orElse(null);

        // Max HE Grenade kills (Bomberman)
        String bomberman = games.stream()
                .flatMap(g -> g.getPlayers().values().stream())
                .filter(p -> p.getTeam() != Team.SPECTATOR)
                .collect(java.util.stream.Collectors.groupingBy(Player::getName,
                        java.util.stream.Collectors
                                .summingInt(p -> p.getKillsByWeapon().getOrDefault("UT_MOD_HEGRENADE", 0))))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .filter(e -> e.getValue() > 0)
                .map(Map.Entry::getKey).orElse(null);

        String quechua = games.stream()
                .flatMap(g -> g.getPlayers().values().stream())
                .filter(p -> p.getTeam() != Team.SPECTATOR)
                .collect(java.util.stream.Collectors.groupingBy(Player::getName,
                        java.util.stream.Collectors
                                .summingInt(p -> p.getKillsByWeapon().getOrDefault("UT_MOD_G36", 0))))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(null);

        // Best K/D (min 10 kills)
        String sharpshooter = totalKills.entrySet().stream()
                .filter(e -> e.getValue() >= 10)
                .max((e1, e2) -> {
                    double kd1 = (double) e1.getValue() / Math.max(1, totalDeaths.getOrDefault(e1.getKey(), 1));
                    double kd2 = (double) e2.getValue() / Math.max(1, totalDeaths.getOrDefault(e2.getKey(), 1));
                    return Double.compare(kd1, kd2);
                })
                .map(Map.Entry::getKey).orElse(null);

        // Assign badges (store keys instead of formatted text)
        if (topKiller != null) {
            playerAchievements.computeIfAbsent(topKiller, k -> new ArrayList<>())
                    .add("topKiller");
        }
        if (sharpshooter != null) {
            playerAchievements.computeIfAbsent(sharpshooter, k -> new ArrayList<>())
                    .add("sharpshooter");
        }
        if (mostFlags != null) {
            playerAchievements.computeIfAbsent(mostFlags, k -> new ArrayList<>())
                    .add("champion");
        }
        if (grimReaper != null) {
            playerAchievements.computeIfAbsent(grimReaper, k -> new ArrayList<>())
                    .add("grimReaper");
        }
        if (tank != null) {
            playerAchievements.computeIfAbsent(tank, k -> new ArrayList<>())
                    .add("tank");
        }
        if (theBrain != null) {
            playerAchievements.computeIfAbsent(theBrain, k -> new ArrayList<>())
                    .add("theBrain");
        }
        if (quechua != null) {
            playerAchievements.computeIfAbsent(quechua, k -> new ArrayList<>())
                    .add("quechua");
        }
        if (lemming != null) {
            playerAchievements.computeIfAbsent(lemming, k -> new ArrayList<>())
                    .add("lemming");
        }
        if (bomberman != null) {
            playerAchievements.computeIfAbsent(bomberman, k -> new ArrayList<>())
                    .add("bomberman");
        }

        return playerAchievements;
    }

    /**
     * Creates a body hit diagram showing the distribution of hits by body part.
     *
     * @param bodyPartHits map of body part names to hit counts
     * @return HTML div containing the body diagram
     */
    /**
     * Creates a body hit diagram showing the distribution of hits by body part.
     * Uses a "Tactical HUD" layout with leader lines connecting stats to body
     * parts.
     *
     * @param bodyPartHits map of body part names to hit counts
     * @return HTML div containing the body diagram
     */
    private DomContent createBodyDiagram(Map<String, Integer> bodyPartHits) {
        if (bodyPartHits.isEmpty()) {
            return div(); // Return empty div if no data
        }

        int totalHits = bodyPartHits.values().stream().mapToInt(Integer::intValue).sum();

        // Calculate percentages for each body part
        int helmetHits = bodyPartHits.getOrDefault("Helmet", 0) + bodyPartHits.getOrDefault("Head", 0);
        int kevlarHits = bodyPartHits.getOrDefault("Kevlar", 0) + bodyPartHits.getOrDefault("Torso", 0)
                + bodyPartHits.getOrDefault("Body", 0);
        int armsHits = bodyPartHits.getOrDefault("Arms", 0);
        int legsHits = bodyPartHits.getOrDefault("Legs", 0);

        double helmetPct = totalHits > 0 ? (helmetHits * 100.0 / totalHits) : 0;
        double kevlarPct = totalHits > 0 ? (kevlarHits * 100.0 / totalHits) : 0;
        double armsPct = totalHits > 0 ? (armsHits * 100.0 / totalHits) : 0;
        double legsPct = totalHits > 0 ? (legsHits * 100.0 / totalHits) : 0;

        return div(
                p(text(Messages.get("bodyHit.totalHits", String.valueOf(totalHits))))
                        .withClass("body-hit-total"),
                div(
                        // Left Column: Arms
                        div(
                                createHudCard("Arms", armsHits, armsPct, "left")).withClass("hud-column left-col"),

                        // Center Column: Silhouette & Leader Lines
                        div(rawHtml(createHudSvg())).withClass("hud-column center-col"),

                        // Right Column: Head, Torso, Legs
                        div(
                                createHudCard("Head", helmetHits, helmetPct, "right"),
                                createHudCard("Torso", kevlarHits, kevlarPct, "right"),
                                createHudCard("Legs", legsHits, legsPct, "right")).withClass("hud-column right-col"))
                        .withClass("hud-container"))
                .withClass("body-hit-section");
    }

    private DomContent createHudCard(String type, int hits, double pct, String side) {
        String labelKey = "bodyHit.helmet"; // Default
        if (type.equals("Torso"))
            labelKey = "bodyHit.kevlar";
        if (type.equals("Arms"))
            labelKey = "bodyHit.arms";
        if (type.equals("Legs"))
            labelKey = "bodyHit.legs";

        return div(
                div(Messages.get(labelKey)).withClass("hud-label"),
                div(
                        span(String.format("%.1f%%", pct)).withClass("hud-pct"),
                        span(" (" + hits + ")").withClass("hud-count")).withClass("hud-values"))
                .withClass("hud-card hud-" + side + " hud-" + type.toLowerCase())
                .attr("data-part", type.toLowerCase());
    }

    private String createHudSvg() {
        return "<svg class=\"hud-svg\" viewBox=\"0 0 300 400\" xmlns=\"http://www.w3.org/2000/svg\">" +
                "  <defs>" +
                "    <filter id=\"glow\" x=\"-20%\" y=\"-20%\" width=\"140%\" height=\"140%\">" +
                "      <feGaussianBlur stdDeviation=\"2\" result=\"blur\"/>" +
                "      <feComposite in=\"SourceGraphic\" in2=\"blur\" operator=\"over\"/>" +
                "    </filter>" +
                "  </defs>" +

                "  <!-- Silhouette (Ghostly Wireframe) -->" +
                "  <g class=\"silhouette-group\" filter=\"url(#glow)\">" +
                "    <ellipse cx=\"150\" cy=\"50\" rx=\"25\" ry=\"30\" class=\"body-part body-head\" data-part=\"head\" />"
                +
                "    <path d=\"M120,85 L180,85 L170,220 L130,220 Z\" class=\"body-part body-torso\" data-part=\"torso\" />"
                +
                "    <rect x=\"80\" y=\"90\" width=\"30\" height=\"100\" rx=\"5\" class=\"body-part body-arms\" data-part=\"arms\" />"
                +
                "    <rect x=\"190\" y=\"90\" width=\"30\" height=\"100\" rx=\"5\" class=\"body-part body-arms\" data-part=\"arms\" />"
                +
                "    <rect x=\"125\" y=\"225\" width=\"22\" height=\"130\" rx=\"5\" class=\"body-part body-legs\" data-part=\"legs\" />"
                +
                "    <rect x=\"153\" y=\"225\" width=\"22\" height=\"130\" rx=\"5\" class=\"body-part body-legs\" data-part=\"legs\" />"
                +
                "  </g>" +

                "  <!-- Leader Lines (Connecting text areas to body parts) -->" +
                "  <g class=\"leader-lines\">" +
                "    <!-- Head (Right side) -->" +
                "    <polyline points=\"175,50 220,50 280,50\" class=\"line line-head\" />" +
                "    <circle cx=\"175\" cy=\"50\" r=\"3\" class=\"dot\" />" +

                "    <!-- Torso (Right side) -->" +
                "    <polyline points=\"170,140 220,140 280,140\" class=\"line line-torso\" />" +
                "    <circle cx=\"170\" cy=\"140\" r=\"3\" class=\"dot\" />" +

                "    <!-- Arms (Left side - pointing to left arm) -->" +
                "    <polyline points=\"80,140 40,140 10,140\" class=\"line line-arms\" />" +
                "    <circle cx=\"80\" cy=\"140\" r=\"3\" class=\"dot\" />" +

                "    <!-- Legs (Right side - pointing to right thigh/knee) -->" +
                "    <polyline points=\"175,280 220,280 280,280\" class=\"line line-legs\" />" +
                "    <circle cx=\"175\" cy=\"280\" r=\"3\" class=\"dot\" />" +
                "  </g>" +
                "</svg>";
    }

    private ContainerTag<?> thHeader(String title, int colIndex, String tableId) {
        return th(
                text(title + " "),
                span().withClass("sort-arrow"))
                .attr("onclick", "sortTable(" + colIndex + ", '" + tableId + "')")
                .withClass("clickable-header");
    }

    DomContent generateScoreTableHTML(List<Game> gamesToAggregate, String tableId,
            Map<String, String> playerColors, boolean showPerGameStats, Map<String, List<String>> achievements) {
        Map<String, Map<String, Double>> aggregated = new HashMap<>();
        Map<String, Integer> gamesPlayed = new HashMap<>();
        for (Game game : gamesToAggregate) {
            for (Player p : game.getPlayers().values()) {
                // Skip spectators
                if (p.getTeam() == Team.SPECTATOR) {
                    continue;
                }
                String name = p.getName();
                gamesPlayed.put(name, gamesPlayed.getOrDefault(name, 0) + 1);
            }
        }
        for (Game game : gamesToAggregate) {
            for (Player p : game.getPlayers().values()) {
                // Skip spectators
                if (p.getTeam() == Team.SPECTATOR) {
                    continue;
                }
                String name = p.getName();
                aggregated.putIfAbsent(name, new HashMap<>());
                Map<String, Double> stats = aggregated.get(name);
                stats.merge("score", (double) p.getScore(), Double::sum);
                stats.merge("kills", (double) p.getKills(), Double::sum);
                stats.merge("deaths", (double) p.getDeaths(), Double::sum);
                stats.merge("suicides", (double) p.getSuicides(), Double::sum);
                stats.merge("worldKills", (double) p.getKilledByWorld(), Double::sum);
                stats.merge("flagCaptures", (double) p.getFlagCaptures(), Double::sum);
                stats.merge("flagReturns", (double) p.getFlagReturns(), Double::sum);
                stats.merge("teamKills", (double) p.getTeamKills(), Double::sum);
                stats.merge("killedByTeammates", (double) p.getKilledByTeammates(), Double::sum);
                stats.merge("hitsGiven", (double) p.getHitsGiven(), Double::sum);
                stats.merge("hitsReceived", (double) p.getHitsReceived(), Double::sum);
                stats.merge("damageGiven", (double) p.getDamageGiven(), Double::sum);
                stats.merge("damageReceived", (double) p.getDamageReceived(), Double::sum);
                stats.merge("maxKillStreak", (double) p.getMaxKillStreak(), Math::max);
                stats.merge("maxDeathStreak", (double) p.getMaxDeathStreak(), Math::max);
            }
        }
        // Sort by Score/Game descending
        List<Map.Entry<String, Map<String, Double>>> sortedEntries = new ArrayList<>(aggregated.entrySet());
        sortedEntries.sort((e1, e2) -> {
            String name1 = e1.getKey();
            String name2 = e2.getKey();
            int games1 = gamesPlayed.getOrDefault(name1, 0);
            int games2 = gamesPlayed.getOrDefault(name2, 0);
            double scorePerGame1 = games1 == 0 ? 0 : e1.getValue().getOrDefault("score", 0.0) / games1;
            double scorePerGame2 = games2 == 0 ? 0 : e2.getValue().getOrDefault("score", 0.0) / games2;
            return Double.compare(scorePerGame2, scorePerGame1); // Descending
        });

        // Determine if this is the main scores table to apply default sorting indicator
        boolean isMainTable = "scoresTable".equals(tableId);

        // Build headers dynamically
        List<DomContent> headers = new ArrayList<>();
        int col = 0;

        headers.add(thHeader(Messages.get("header.name"), col++, tableId).withClass("nowrap"));
        headers.add(thHeader(Messages.get("header.totalScore"), col++, tableId));

        if (showPerGameStats) {
            headers.add(thHeader(Messages.get("header.gamesPlayed"), col++, tableId));
        }

        headers.add(thHeader(Messages.get("header.kills"), col++, tableId));
        headers.add(thHeader(Messages.get("header.deaths"), col++, tableId));
        headers.add(thHeader(Messages.get("header.suicides"), col++, tableId));
        headers.add(thHeader(Messages.get("header.environmentDeaths"), col++, tableId));
        headers.add(thHeader(Messages.get("header.flagsCaptured"), col++, tableId));
        headers.add(thHeader(Messages.get("header.flagsReturned"), col++, tableId));
        headers.add(thHeader(Messages.get("header.teamKills"), col++, tableId));
        headers.add(thHeader(Messages.get("header.killedByTeam"), col++, tableId));

        if (showPerGameStats) {
            headers.add(thHeader(Messages.get("header.scorePerGame"), col++, tableId)
                    .with(isMainTable ? span(rawHtml(" &#9660;")).withClass("sort-arrow")
                            : span().withClass("sort-arrow")));
            headers.add(thHeader(Messages.get("header.killsPerGame"), col++, tableId));
            headers.add(thHeader(Messages.get("header.flagsPerGame"), col++, tableId));
        }

        headers.add(thHeader(Messages.get("header.killDeathRatio"), col++, tableId));
        headers.add(thHeader(Messages.get("header.bestKillStreak"), col++, tableId));
        headers.add(thHeader(Messages.get("header.worstDeathStreak"), col++, tableId));
        headers.add(thHeader(Messages.get("header.hitsGiven"), col++, tableId));
        headers.add(thHeader(Messages.get("header.hitsReceived"), col++, tableId));
        headers.add(thHeader(Messages.get("header.damageGiven"), col++, tableId));
        headers.add(thHeader(Messages.get("header.damageReceived"), col++, tableId));

        return div(
                table(
                        thead(tr(headers.toArray(new DomContent[0]))),
                        tbody(
                                each(sortedEntries, entry -> {
                                    String name = entry.getKey();
                                    Map<String, Double> stats = entry.getValue();
                                    int gamesPlayedByPlayer = gamesPlayed.getOrDefault(name, 0);
                                    double totalScore = stats.getOrDefault("score", 0.0);
                                    double totalKills = stats.getOrDefault("kills", 0.0);
                                    double totalDeaths = stats.getOrDefault("deaths", 0.0);
                                    double flagReturns = stats.getOrDefault("flagReturns", 0.0);
                                    String playerColor = playerColors.getOrDefault(name, "hsl(0, 0%, 50%)");

                                    List<DomContent> cells = new ArrayList<>();

                                    // Build player name cell with badges below
                                    DivTag nameWrapper = div();

                                    // Name with color square on same line
                                    DivTag nameLine = div(
                                            getColorSquare(playerColor),
                                            text(name)).withClass("player-name-line");
                                    nameWrapper.with(nameLine);

                                    // Add badges if player has achievements (displayed below name)
                                    List<String> playerBadges = achievements.getOrDefault(name,
                                            Collections.emptyList());
                                    if (!playerBadges.isEmpty()) {
                                        DivTag badgesLine = div().withClass("player-badges-line");
                                        for (String badgeKey : playerBadges) {
                                            // Map badge keys to icons
                                            String icon = "";
                                            switch (badgeKey) {
                                                case "topKiller":
                                                    icon = "👑";
                                                    break;
                                                case "sharpshooter":
                                                    icon = "🎯";
                                                    break;
                                                case "champion":
                                                    icon = "🏆";
                                                    break;
                                                case "grimReaper":
                                                    icon = "💀";
                                                    break;
                                                case "tank":
                                                    icon = "🛡️";
                                                    break;
                                                case "theBrain":
                                                    icon = "🧠";
                                                    break;
                                                case "quechua":
                                                    icon = "⛺";
                                                    break;
                                                case "lemming":
                                                    icon = "🐹";
                                                    break;
                                                case "bomberman":
                                                    icon = "💣";
                                                    break;
                                            }

                                            String badgeName = Messages.get("badge." + badgeKey);
                                            String badgeDesc = Messages.get("badge." + badgeKey + ".desc");

                                            badgesLine.with(
                                                    span(text(icon + " " + badgeName))
                                                            .withClass("badge")
                                                            .withTitle(badgeDesc));
                                        }
                                        nameWrapper.with(badgesLine);
                                    }

                                    cells.add(td(nameWrapper)
                                            .withClass("nowrap player-name-link")
                                            .attr("onclick", "openPlayerInUsersTab('"
                                                    + name.replace("'", "\\'") + "')"));
                                    cells.add(td(String.valueOf((int) totalScore)));

                                    if (showPerGameStats) {
                                        cells.add(td(String.valueOf(gamesPlayedByPlayer)));
                                    }

                                    cells.add(td(String.valueOf((int) totalKills)));
                                    cells.add(td(String.valueOf((int) totalDeaths)));
                                    cells.add(td(String.valueOf(stats.getOrDefault("suicides", 0.0).intValue())));
                                    cells.add(td(String.valueOf(stats.getOrDefault("worldKills", 0.0).intValue())));
                                    cells.add(td(String.valueOf(stats.getOrDefault("flagCaptures", 0.0).intValue())));
                                    cells.add(td(String.valueOf((int) flagReturns)));
                                    cells.add(td(String.valueOf(stats.getOrDefault("teamKills", 0.0).intValue())));
                                    cells.add(td(
                                            String.valueOf(stats.getOrDefault("killedByTeammates", 0.0).intValue())));

                                    if (showPerGameStats) {
                                        cells.add(td(String.format("%.2f", gamesPlayedByPlayer == 0 ? 0.0
                                                : totalScore / gamesPlayedByPlayer)));
                                        cells.add(td(String.format("%.2f", gamesPlayedByPlayer == 0 ? 0.0
                                                : totalKills / gamesPlayedByPlayer)));
                                        cells.add(td(String.format("%.2f", gamesPlayedByPlayer == 0 ? 0.0
                                                : flagReturns / gamesPlayedByPlayer)));
                                    }

                                    cells.add(td(String.format("%.2f", totalDeaths == 0 ? totalKills
                                            : totalKills / totalDeaths)));
                                    cells.add(td(String.valueOf(stats.getOrDefault("maxKillStreak", 0.0).intValue())));
                                    cells.add(td(String.valueOf(stats.getOrDefault("maxDeathStreak", 0.0).intValue())));
                                    cells.add(td(String.valueOf(stats.getOrDefault("hitsGiven", 0.0).intValue())));
                                    cells.add(td(String.valueOf(stats.getOrDefault("hitsReceived", 0.0).intValue())));
                                    cells.add(td(String.valueOf(stats.getOrDefault("damageGiven", 0.0).intValue())));
                                    cells.add(td(String.valueOf(stats.getOrDefault("damageReceived", 0.0).intValue())));

                                    return tr(cells.toArray(new DomContent[0]));
                                })))
                        .withId(tableId))
                .withClass("table-wrapper");
    }

    private ScriptTag createChartScript(List<Game> games, Map<String, String> playerColors) {
        StringBuilder script = new StringBuilder();
        script.append("document.addEventListener('DOMContentLoaded', function() {\n");

        // Inject i18n messages
        script.append("  window.i18n = {\n");
        script.append("    favoriteVictim: '" + Messages.get("user.favoriteVictim") + "',\n");
        script.append("    nemesis: '" + Messages.get("user.nemesis") + "',\n");
        script.append("    favoriteWeapons: '" + Messages.get("user.favoriteWeapons") + "',\n");
        script.append("    weapon: '" + Messages.get("user.weapon") + "',\n");
        script.append("    kills: '" + Messages.get("user.kills") + "'\n");
        script.append("  };\n");

        Set<String> allPlayerNamesSet = new HashSet<>();
        for (Game game : games) {
            for (Player p : game.getPlayers().values()) {
                allPlayerNamesSet.add(p.getName());
            }
        }
        List<String> allPlayerNames = new ArrayList<>(allPlayerNamesSet);
        Collections.sort(allPlayerNames);

        // --- Calculate Global Stats for Global Charts ---
        Map<String, Double> globalKD = new HashMap<>();
        Map<String, Integer> globalFlags = new HashMap<>();

        for (String playerName : allPlayerNames) {
            int kills = 0;
            int deaths = 0;
            int flags = 0;
            for (Game game : games) {
                for (Player p : game.getPlayers().values()) {
                    if (p.getName().equals(playerName)) {
                        kills += p.getKills();
                        deaths += p.getDeaths();
                        flags += p.getFlagCaptures();
                    }
                }
            }
            double kd = (deaths == 0) ? kills : (double) kills / deaths;
            globalKD.put(playerName, kd);
            if (flags > 0) {
                globalFlags.put(playerName, flags);
            }
        }

        // --- Generate Global K/D Chart ---
        script.append("  const kdCtx = document.getElementById('globalKDChart');\n");
        script.append("  if (kdCtx) {\n");
        script.append("    new Chart(kdCtx, {\n");
        script.append("      type: 'bar',\n");
        script.append("      data: {\n");
        script.append("        labels: [");
        // Sort KD by value descending
        List<Map.Entry<String, Double>> sortedKD = new ArrayList<>(globalKD.entrySet());
        sortedKD.sort(Map.Entry.<String, Double>comparingByValue().reversed());
        for (int i = 0; i < sortedKD.size(); i++) {
            script.append("'").append(sortedKD.get(i).getKey().replace("'", "\\'")).append("'")
                    .append(i < sortedKD.size() - 1 ? ", " : "");
        }
        script.append("],\n");
        script.append("        datasets: [{\n");
        script.append("          label: '").append(Messages.get("header.killDeathRatio")).append("',\n");
        script.append("          data: [");
        for (int i = 0; i < sortedKD.size(); i++) {
            script.append(String.format("%.2f", sortedKD.get(i).getValue()).replace(",", "."))
                    .append(i < sortedKD.size() - 1 ? ", " : "");
        }
        script.append("],\n");
        script.append("          backgroundColor: [");
        for (int i = 0; i < sortedKD.size(); i++) {
            script.append("'").append(playerColors.getOrDefault(sortedKD.get(i).getKey(), "hsl(0,0%,50%)"))
                    .append("'").append(i < sortedKD.size() - 1 ? ", " : "");
        }
        script.append("]\n");
        script.append("        }]\n");
        script.append("      },\n");
        script.append("      options: { \n");
        script.append("        responsive: true, \n");
        script.append("        maintainAspectRatio: false, \n");
        script.append("        plugins: { legend: { display: false } } \n");
        script.append("      }\n");
        script.append("    });\n");
        script.append("  }\n");

        // --- Generate Global Flags Chart ---
        if (!globalFlags.isEmpty()) {
            script.append("  const flagsCtx = document.getElementById('globalFlagsChart');\n");
            script.append("  if (flagsCtx) {\n");
            script.append("    new Chart(flagsCtx, {\n");
            script.append("      type: 'bar',\n");
            script.append("      data: {\n");
            script.append("        labels: [");
            // Sort Flags by value descending
            List<Map.Entry<String, Integer>> sortedFlags = new ArrayList<>(globalFlags.entrySet());
            sortedFlags.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
            for (int i = 0; i < sortedFlags.size(); i++) {
                script.append("'").append(sortedFlags.get(i).getKey().replace("'", "\\'")).append("'")
                        .append(i < sortedFlags.size() - 1 ? ", " : "");
            }
            script.append("],\n");
            script.append("        datasets: [{\n");
            script.append("          label: '").append(Messages.get("header.flagsCaptured")).append("',\n");
            script.append("          data: [");
            for (int i = 0; i < sortedFlags.size(); i++) {
                script.append(sortedFlags.get(i).getValue())
                        .append(i < sortedFlags.size() - 1 ? ", " : "");
            }
            script.append("],\n");
            script.append("          backgroundColor: [");
            for (int i = 0; i < sortedFlags.size(); i++) {
                script.append("'")
                        .append(playerColors.getOrDefault(sortedFlags.get(i).getKey(), "hsl(0,0%,50%)"))
                        .append("'").append(i < sortedFlags.size() - 1 ? ", " : "");
            }
            script.append("]\n");
            script.append("        }]\n");
            script.append("      },\n");
            script.append("      options: { \n");
            script.append("        responsive: true, \n");
            script.append("        maintainAspectRatio: false, \n");
            script.append("        plugins: { legend: { display: false } } \n");
            script.append("      }\n");
            script.append("    });\n");
            script.append("  }\n");
        }

        // Set global Chart.js defaults for dark theme
        script.append("  Chart.defaults.color = '#e0e0e0';\n");
        script.append("  Chart.defaults.borderColor = '#444';\n");

        // Serialize player stats
        script.append("  window.playerStats = {};\n");
        for (String playerName : allPlayerNames) {
            Map<String, Integer> killsAgainst = new HashMap<>();
            Map<String, Integer> deathsBy = new HashMap<>();
            Map<String, Integer> weapons = new HashMap<>();
            List<Map<String, Object>> scoresPerGame = new ArrayList<>();
            List<Integer> killsPerGame = new ArrayList<>();
            List<Integer> deathsPerGame = new ArrayList<>();

            for (Game game : games) {
                for (Player p : game.getPlayers().values()) {
                    if (p.getName().equals(playerName)) {
                        if (p.getTeam() != Team.SPECTATOR) {
                            Map<String, Object> gameData = new HashMap<>();
                            gameData.put("score", (int) p.getScore());
                            gameData.put("map", Messages.getMapName(game.getMapName()));
                            scoresPerGame.add(gameData);
                            killsPerGame.add(p.getKills());
                            deathsPerGame.add(p.getDeaths());
                        }
                        p.getKillsByVictim().forEach(
                                (k, v) -> killsAgainst.merge(k, v, Integer::sum));
                        p.getDeathsByKiller()
                                .forEach((k, v) -> deathsBy.merge(k, v, Integer::sum));
                        p.getKillsByWeapon()
                                .forEach((k, v) -> weapons.merge(Messages.get("weapon." + k), v, Integer::sum));
                    }
                }
            }

            String favoriteVictim = killsAgainst.entrySet().stream().max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey).orElse(Messages.get("user.na"));
            int favoriteVictimKills = killsAgainst.getOrDefault(favoriteVictim, 0);

            String nemesis = deathsBy.entrySet().stream().max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey).orElse(Messages.get("user.na"));
            int nemesisKills = deathsBy.getOrDefault(nemesis, 0);

            script.append("  window.playerStats['").append(playerName.replace("'", "\\'"))
                    .append("'] = {\n");
            script.append("    favoriteVictim: '").append(favoriteVictim.replace("'", "\\'"))
                    .append("',\n");
            script.append("    favoriteVictimKills: ").append(favoriteVictimKills).append(",\n");
            script.append("    nemesis: '").append(nemesis.replace("'", "\\'")).append("',\n");
            script.append("    nemesisKills: ").append(nemesisKills).append(",\n");

            // Serialize kills against data for chart
            script.append("    killsAgainst: {");
            int killIndex = 0;
            List<Map.Entry<String, Integer>> sortedKills = new ArrayList<>(killsAgainst.entrySet());
            sortedKills.sort(Map.Entry.<String, Integer>comparingByValue().reversed());

            for (Map.Entry<String, Integer> entry : sortedKills) {
                if (killIndex > 0) {
                    script.append(", ");
                }
                script.append("'").append(entry.getKey().replace("'", "\\'")).append("': ")
                        .append(entry.getValue());
                killIndex++;
            }
            script.append("},\n");

            script.append("    weapons: [\n");

            weapons.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(e -> {
                        script.append("      { name: '").append(e.getKey().replace("'", "\\'"))
                                .append("', kills: ").append(e.getValue())
                                .append(" },\n");
                    });

            script.append("    ],\n");

            // Add scoresPerGame array with map names
            script.append("    scoresPerGame: [");
            for (int i = 0; i < scoresPerGame.size(); i++) {
                if (i > 0)
                    script.append(", ");
                Map<String, Object> gameData = scoresPerGame.get(i);
                script.append("{ score: ").append(gameData.get("score"))
                        .append(", map: '").append(((String) gameData.get("map")).replace("'", "\\'")).append("' }");
            }
            script.append("],\n");

            // Add killsPerGame and deathsPerGame arrays
            script.append("    killsPerGame: [");
            for (int i = 0; i < killsPerGame.size(); i++) {
                if (i > 0)
                    script.append(", ");
                script.append(killsPerGame.get(i));
            }
            script.append("],\n");

            script.append("    deathsPerGame: [");
            for (int i = 0; i < deathsPerGame.size(); i++) {
                if (i > 0)
                    script.append(", ");
                script.append(deathsPerGame.get(i));
            }
            script.append("]\n");

            script.append("  };\n");
        }

        // Serialize player colors
        script.append("  window.playerColors = {\n");
        int colorIndex = 0;
        for (Map.Entry<String, String> entry : playerColors.entrySet()) {
            if (colorIndex > 0) {
                script.append(",\n");
            }
            script.append("    '").append(entry.getKey().replace("'", "\\'"))
                    .append("': '").append(entry.getValue()).append("'");
            colorIndex++;
        }
        script.append("\n  };\n");

        // Call the chart initialization function
        script.append("  initializeCharts();\n");
        script.append("});\n");
        return script(rawHtml(script.toString()));
    }

    private String loadResource(String resourceName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourceName);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
