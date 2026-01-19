package fr.adrienlombard.utlogs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

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
