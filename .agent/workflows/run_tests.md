---
description: Build and run the application against all log files in src/main/resources, outputting to test_output
---
1. Build the project
// turbo
mvn package -DskipTests

2. Run the application
// turbo
java -cp target/utlogs-1.0-SNAPSHOT-jar-with-dependencies.jar fr.adrienlombard.App -r src/main/resources -o test_output

3. Verify output
// turbo
ls -l test_output
