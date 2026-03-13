package fr.adrienlombard.utlogs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @Test
    public void testParseArguments_NoArgs() {
        assertNull(App.parseArguments(new String[] {}));
    }

    @Test
    public void testParseArguments_SingleFile() {
        try {
            java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test", ".log");
            tempFile.toFile().deleteOnExit();

            App.Config config = App.parseArguments(new String[] { tempFile.toString() });
            assertNotNull(config);
            assertEquals(tempFile.toAbsolutePath(), config.input.toAbsolutePath());
            assertNull(config.outputDir);
            assertFalse(config.recursive);
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testParseArguments_DirectoryWithR() {
        try {
            java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("testDir");
            tempDir.toFile().deleteOnExit();

            App.Config config = App.parseArguments(new String[] { "-r", tempDir.toString() });
            assertNotNull(config);
            assertEquals(tempDir.toAbsolutePath(), config.input.toAbsolutePath());
            assertNull(config.outputDir);
            assertTrue(config.recursive);
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testParseArguments_MissingRArg() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            App.parseArguments(new String[] { "-r" });
        });
        assertTrue(exception.getMessage().contains("Missing directory path")
                || exception.getMessage().contains("Missing path"));
    }

    @Test
    public void testParseArguments_InvalidPath() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            App.parseArguments(new String[] { "non_existent_file.log" });
        });
        assertTrue(exception.getMessage().contains("does not exist"));
    }

    @Test
    public void testParseArguments_DirectoryWithoutR() {
        try {
            java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("testDir");
            tempDir.toFile().deleteOnExit();

            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                App.parseArguments(new String[] { tempDir.toString() });
            });
            assertTrue(exception.getMessage().contains("Input is a directory"));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testParseArguments_FileWithR() {
        try {
            java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test", ".log");
            tempFile.toFile().deleteOnExit();

            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                App.parseArguments(new String[] { "-r", tempFile.toString() });
            });
            assertTrue(exception.getMessage().contains("Input is a file"));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testParseArguments_WithOutputDirectory() {
        try {
            java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test", ".log");
            tempFile.toFile().deleteOnExit();
            java.nio.file.Path outputDir = java.nio.file.Paths.get("output");

            App.Config config = App.parseArguments(new String[] { tempFile.toString(), "-o", outputDir.toString() });
            assertNotNull(config);
            assertEquals(tempFile.toAbsolutePath(), config.input.toAbsolutePath());
            assertEquals(outputDir, config.outputDir);
            assertFalse(config.recursive);
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testParseArguments_WithOutputDirectory_Recursive() {
        try {
            java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("testDir");
            tempDir.toFile().deleteOnExit();
            java.nio.file.Path outputDir = java.nio.file.Paths.get("output");

            App.Config config = App
                    .parseArguments(new String[] { "-r", tempDir.toString(), "-o", outputDir.toString() });
            assertNotNull(config);
            assertEquals(tempDir.toAbsolutePath(), config.input.toAbsolutePath());
            assertEquals(outputDir, config.outputDir);
            assertTrue(config.recursive);
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testParseArguments_MissingOutputDirectory() {
        try {
            java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test", ".log");
            tempFile.toFile().deleteOnExit();

            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                App.parseArguments(new String[] { tempFile.toString(), "-o" });
            });
            assertTrue(exception.getMessage().contains("Erreur : Répertoire de sortie manquant"));
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
}
