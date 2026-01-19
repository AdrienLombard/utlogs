package fr.adrienlombard.utlogs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

class GameTest {

    @Test
    public void testInitialState() {
        Game game = new Game();
        assertEquals("Unknown Map", game.getMapName());
        assertEquals(0, game.getTotalKills());
        assertTrue(game.getPlayers().isEmpty());
    }

    @Test
    public void testSetMapName() {
        Game game = new Game();
        game.setMapName("ut4_test");
        assertEquals("ut4_test", game.getMapName());
    }

    @Test
    public void testGetPlayer() {
        Game game = new Game();
        Player p1 = game.getPlayerByName("Player1");
        assertNotNull(p1);
        assertEquals("Player1", p1.getName());

        Player p2 = game.getPlayerByName("Player1");
        assertSame(p1, p2); // Should return same instance

        // Test ID mapping
        game.registerPlayerId("1", "Player1");
        Player p3 = game.getPlayerById("1");
        assertSame(p1, p3);
    }

    @Test
    public void testAddKill() {
        Game game = new Game();
        game.addKill();
        assertEquals(1, game.getTotalKills());
    }

    @Test
    public void testGameType() {
        Game game = new Game();
        assertEquals("0", game.getGameType()); // Default
        game.setGameType("7");
        assertEquals("7", game.getGameType());
    }

    @Test
    public void testToString() {
        Game game = new Game();
        game.setMapName("test_map");
        game.addKill();
        String str = game.toString();
        assertTrue(str.contains("mapName='test_map'"));
        assertTrue(str.contains("totalKills=1"));
    }

    @Test
    public void testGetPlayersUnmodifiable() {
        Game game = new Game();
        Map<String, Player> players = game.getPlayers();
        assertThrows(UnsupportedOperationException.class, () -> {
            players.put("new", new Player("test"));
        });
    }
}
