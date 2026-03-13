package fr.adrienlombard.utlogs;

/**
 * Hello world!
 *
 */
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class App {
    private static final String LOG_PREFIX = "games.log";

    public static void main(String[] args) {
        // Set default locale to French
        Messages.setLocale(java.util.Locale.FRENCH);

        try {
            Config config = parseArguments(args);
            if (config == null) {
                return;
            }

            List<Path> logFiles = getLogFiles(config.input);

            if (logFiles.isEmpty()) {
                System.out.println(Messages.get("process.noLogFiles", config.input));
                return;
            }

            processLogFiles(logFiles, config.outputDir);

        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(Messages.get("error.accessingFiles", e.getMessage()));
            e.printStackTrace();
        }
    }

    protected static class Config {
        Path input;
        Path outputDir;
        boolean recursive;

        Config(Path input, Path outputDir, boolean recursive) {
            this.input = input;
            this.outputDir = outputDir;
            this.recursive = recursive;
        }
    }

    protected static Config parseArguments(String[] args) {
        if (args.length == 0) {
            System.out.println(Messages.get("usage.message"));
            return null;
        }

        Path input = null;
        Path outputDir = null;
        boolean recursive = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-r")) {
                recursive = true;
            } else if (arg.equals("-o")) {
                if (i + 1 < args.length) {
                    outputDir = Paths.get(args[i + 1]);
                    i++; // Skip next arg
                } else {
                    throw new IllegalArgumentException(Messages.get("error.missingOutputDirectory"));
                }
            } else {
                if (input == null) {
                    input = Paths.get(arg);
                } else {
                    // Already have input, maybe throw error or ignore?
                    // For now, assume only one input path
                }
            }
        }

        if (input == null) {
            if (recursive && args.length > 1 && !args[1].startsWith("-")) {
                // Handle case where -r is first and path is second (legacy support if needed,
                // but loop handles it)
                // The loop logic above handles -r flag. If input is still null, it means no
                // path provided.
                // However, the original logic expected `java App -r path`.
                // My loop handles `java App -r path` correctly:
                // i=0: -r -> recursive=true
                // i=1: path -> input=path
            }
            if (input == null) {
                // Try to find a non-flag argument if I missed it?
                // No, the loop covers it.
                // But wait, what if the user runs `java App -r` without path?
                throw new IllegalArgumentException(Messages.get("error.missingPath"));
            }
        }

        if (!Files.exists(input)) {
            throw new IllegalArgumentException(Messages.get("error.inputNotExist", input.toString()));
        }

        if (Files.isDirectory(input)) {
            if (!recursive) {
                throw new IllegalArgumentException(Messages.get("error.directoryNeedsR"));
            }
        } else {
            if (recursive) {
                throw new IllegalArgumentException(Messages.get("error.fileWithR"));
            }
        }

        return new Config(input, outputDir, recursive);
    }

    private static List<Path> getLogFiles(Path input) throws IOException {
        if (Files.isDirectory(input)) {
            try (Stream<Path> stream = Files.list(input)) {
                return stream
                        .filter(p -> p.getFileName().toString().startsWith(LOG_PREFIX))
                        .collect(Collectors.toList());
            }
        } else {
            return List.of(input);
        }
    }

    private static void processLogFiles(List<Path> logFiles, Path outputDir) {
        LogReader reader = new LogReader();
        HtmlGenerator generator = new HtmlGenerator();

        // Create output directory if it doesn't exist
        if (outputDir != null && !Files.exists(outputDir)) {
            try {
                Files.createDirectories(outputDir);
            } catch (IOException e) {
                System.err.println(Messages.get("error.creatingOutputDirectory", e.getMessage()));
                return;
            }
        }

        for (Path logFile : logFiles) {
            try {
                processSingleFile(logFile, reader, generator, outputDir);
            } catch (IOException e) {
                System.err.println(Messages.get("error.processingFile", logFile.getFileName(), e.getMessage()));
                e.printStackTrace();
            }
        }
    }

    private static void processSingleFile(Path logFile, LogReader reader, HtmlGenerator generator, Path outputDir)
            throws IOException {
        System.out.println(Messages.get("process.processing", logFile.getFileName()));
        String dateSuffix = extractDateSuffix(logFile.getFileName().toString());
        String outputFileName = "report_" + dateSuffix + ".html";

        Path outputPath;
        if (outputDir != null) {
            outputPath = outputDir.resolve(outputFileName);
        } else {
            outputPath = Paths.get(outputFileName);
        }

        System.out.println(Messages.get("process.parsing", logFile.toAbsolutePath()));
        List<Game> games = reader.parse(logFile.toAbsolutePath().toString());
        System.out.println(Messages.get("process.foundGames", games.size()));

        System.out.println(Messages.get("process.generating", outputPath.toString()));
        String reportDate = formatDate(dateSuffix);
        generator.generate(games, outputPath.toString(), reportDate);
        System.out.println(Messages.get("process.done", logFile.getFileName()));
        System.out.println("--------------------------------------------------");
    }

    private static String extractDateSuffix(String filename) {
        // Expected format: games.log.YYYY_MM_DD
        // If it matches, return YYYY_MM_DD
        // Otherwise return the full filename to be safe
        if (filename.startsWith(LOG_PREFIX + ".")) {
            return filename.substring(LOG_PREFIX.length() + 1);
        }
        return filename;
    }

    private static String formatDate(String dateSuffix) {
        // Input: YYYY_MM_DD
        // Output: DD/MM/YYYY
        // If input doesn't match expected format, return it as is
        String[] parts = dateSuffix.split("_");
        if (parts.length == 3) {
            return parts[2] + "/" + parts[1] + "/" + parts[0];
        }
        return dateSuffix;
    }
}
