package fr.adrienlombard.utlogs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TeamTest {

    @Test
    public void testFromCode() {
        assertEquals(Team.RED, Team.fromCode("1"));
        assertEquals(Team.BLUE, Team.fromCode("2"));
        assertEquals(Team.FREE, Team.fromCode("0"));
        assertEquals(Team.SPECTATOR, Team.fromCode("3"));
        assertEquals(Team.UNKNOWN, Team.fromCode("999"));
        assertEquals(Team.UNKNOWN, Team.fromCode(""));
        assertEquals(Team.UNKNOWN, Team.fromCode(null));
    }

    @Test
    public void testGetCode() {
        assertEquals("1", Team.RED.getCode());
        assertEquals("2", Team.BLUE.getCode());
    }
}
