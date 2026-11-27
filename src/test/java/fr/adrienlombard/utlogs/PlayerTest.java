package fr.adrienlombard.utlogs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    public void testFlagCaptureTimes() {
        Player player = new Player("TestPlayer");
        assertTrue(player.getFlagCaptureTimesMs().isEmpty());

        player.addFlagCaptureTime(17450);
        player.addFlagCaptureTime(8000);

        assertEquals(java.util.List.of(17450, 8000), player.getFlagCaptureTimesMs());
        // Capture times do not affect the capture counter on their own
        assertEquals(0, player.getFlagCaptures());
    }

    @Test
    public void testFlagCaptureTimesAreUnmodifiable() {
        Player player = new Player("TestPlayer");
        player.addFlagCaptureTime(1000);
        assertThrows(UnsupportedOperationException.class, () -> player.getFlagCaptureTimesMs().add(2000));
    }

    @Test
    public void testAssists() {
        Player player = new Player("TestPlayer");
        assertEquals(0, player.getAssists());
        player.addAssist();
        player.addAssist();
        assertEquals(2, player.getAssists());
        // Assists do not count as kills
        assertEquals(0, player.getKills());
    }

    @Test
    public void testFlagDrops() {
        Player player = new Player("TestPlayer");
        assertEquals(0, player.getFlagDrops());
        player.addFlagDrop();
        assertEquals(1, player.getFlagDrops());
        // Drops are independent from returns and captures
        assertEquals(0, player.getFlagReturns());
        assertEquals(0, player.getFlagCaptures());
    }

    @Test
    public void testSurvivalTimes() {
        Player player = new Player("TestPlayer");
        assertTrue(player.getSurvivalTimesSeconds().isEmpty());
        player.addSurvivalTime(35);
        player.addSurvivalTime(5);
        assertEquals(java.util.List.of(35, 5), player.getSurvivalTimesSeconds());
        // Survival times do not affect the death counter on their own
        assertEquals(0, player.getDeaths());
    }

    @Test
    public void testSurvivalTimesAreUnmodifiable() {
        Player player = new Player("TestPlayer");
        player.addSurvivalTime(10);
        assertThrows(UnsupportedOperationException.class, () -> player.getSurvivalTimesSeconds().add(20));
    }

    @Test
    public void testInitialState() {
        Player player = new Player("TestPlayer");
        assertEquals("TestPlayer", player.getName());
        assertEquals(0, player.getKills());
        assertEquals(0, player.getDeaths());
        assertEquals(Team.UNKNOWN, player.getTeam());
    }

    @Test
    public void testAddKill() {
        Player player = new Player("TestPlayer");
        player.addKill();
        assertEquals(1, player.getKills());
        assertEquals(1, player.getMaxKillStreak());

        player.addKill();
        assertEquals(2, player.getKills());
        assertEquals(2, player.getMaxKillStreak());
    }

    @Test
    public void testAddDeath() {
        Player player = new Player("TestPlayer");
        player.addDeath();
        assertEquals(1, player.getDeaths());
        assertEquals(1, player.getMaxDeathStreak());
    }

    @Test
    public void testKillStreakReset() {
        Player player = new Player("TestPlayer");
        player.addKill();
        player.addKill();
        assertEquals(2, player.getMaxKillStreak());

        player.addDeath();
        player.addKill();
        assertEquals(2, player.getMaxKillStreak()); // Should remain max
    }

    @Test
    public void testScore() {
        Player player = new Player("TestPlayer");
        player.setScore(100);
        assertEquals(100, player.getScore());
    }

    @Test
    public void testSuicides() {
        Player player = new Player("TestPlayer");
        player.addSuicide();
        assertEquals(1, player.getSuicides());
        assertEquals(1, player.getDeaths());
        assertEquals(1, player.getMaxDeathStreak());
    }

    @Test
    public void testKilledByWorld() {
        Player player = new Player("TestPlayer");
        player.addKilledByWorld();
        assertEquals(1, player.getKilledByWorld());
        assertEquals(1, player.getDeaths());
        assertEquals(1, player.getMaxDeathStreak());
    }

    @Test
    public void testFlags() {
        Player player = new Player("TestPlayer");
        player.addFlagCapture();
        assertEquals(1, player.getFlagCaptures());

        player.addFlagReturn();
        assertEquals(1, player.getFlagReturns());
    }

    @Test
    public void testTeamKills() {
        Player player = new Player("TestPlayer");
        player.addTeamKill();
        assertEquals(1, player.getTeamKills());

        player.addKilledByTeammates();
        assertEquals(1, player.getKilledByTeammates());
    }

    @Test
    public void testHits() {
        Player player = new Player("TestPlayer");
        player.addHitGiven();
        assertEquals(1, player.getHitsGiven());

        player.addHitReceived();
        assertEquals(1, player.getHitsReceived());
    }

    @Test
    public void testDamage() {
        Player player = new Player("TestPlayer");
        player.addDamageGiven(50);
        assertEquals(50, player.getDamageGiven());

        player.addDamageReceived(30);
        assertEquals(30, player.getDamageReceived());
    }

    @Test
    public void testTeam() {
        Player player = new Player("TestPlayer");
        player.setTeam(Team.RED);
        assertEquals(Team.RED, player.getTeam());
    }

    @Test
    public void testToString() {
        Player player = new Player("TestPlayer");
        player.setScore(10);
        player.addKill();
        player.setTeam(Team.BLUE);

        String str = player.toString();
        assertTrue(str.contains("name='TestPlayer'"));
        assertTrue(str.contains("kills=1"));
        assertTrue(str.contains("score=10"));
        assertTrue(str.contains("team=BLUE"));
    }

    @Test
    public void testInteractions() {
        Player player = new Player("TestPlayer");
        player.addKillAgainst("Victim1");
        player.addKillAgainst("Victim1");
        player.addKillAgainst("Victim2");

        assertEquals(2, player.getKillsByVictim().get("Victim1"));
        assertEquals(1, player.getKillsByVictim().get("Victim2"));

        player.addDeathBy("Killer1");
        assertEquals(1, player.getDeathsByKiller().get("Killer1"));
    }
}
