# UTLogs

UTLogs is a Java application designed to parse Unreal Tournament 4 (UT4) game log files and generate detailed, interactive HTML reports. It specifically targets Capture The Flag (CTF) game modes, providing in-depth statistics for players, teams, and matches.

## Features

*   **Log Parsing**: Efficiently parses standard UT4 server log files.
*   **Detailed Statistics**: Tracks Kills, Deaths, Suicides, Team Kills, Flag Captures/Returns, Kill/Death Streaks, and Damage dealt/received.
*   **Weapon Usage**: Analyzing performance with specific weapons (e.g., Shock Rifle, Sniper, Flak Cannon).
*   **Interactive Reports**: Generates HTML reports with sortable tables and dynamic charts (using Chart.js).
*   **Multi-View Analysis**:
    *   **Global Scores**: Aggregated statistics across all processed games.
    *   **Score / Map**: Detailed breakdown per map and per individual game.
    *   **Users**: Individual player profiles with "Favorite Victim", "Nemesis", and kill distribution charts.
*   **Internationalization**: Reports are available in French (default) and support i18n.
*   **Modern UI**: Clean, responsive design with player color coding.

## Prerequisites

*   **Java**: JDK 17 or later.
*   **Maven**: Apache Maven 3.6.0 or later.

## Building the Project

To build the project and generate the executable JAR file with dependencies, run the following command in the project root directory:

```bash
mvn package
```

This will create `utlogs-1.0-SNAPSHOT-jar-with-dependencies.jar` in the `target` directory.

## Usage

The application can process a single log file or recursively scan a directory. It also supports specifying a custom output directory.

### Syntax

```bash
java -cp target/utlogs-1.0-SNAPSHOT-jar-with-dependencies.jar App [options] <input_path>
```

### Options

*   `-r`: Recursive mode. Required if `<input_path>` is a directory.
*   `-o <output_directory>`: Specify the directory where HTML reports will be generated. Default is the current directory.

### Examples

**1. Process a single log file:**

```bash
java -cp target/utlogs-1.0-SNAPSHOT-jar-with-dependencies.jar App games.log.2025_11_24
```

**2. Process a directory recursively:**

```bash
java -cp target/utlogs-1.0-SNAPSHOT-jar-with-dependencies.jar App -r /path/to/logs
```

**3. Process files and save reports to a specific folder:**

```bash
java -cp target/utlogs-1.0-SNAPSHOT-jar-with-dependencies.jar App -r ./logs -o ./reports
```

## Output

The application generates an HTML report for each processed log file (or group of files if locally aggregated). The report filename uses the date found in the log filename (e.g., `report_2025_11_24.html`).
