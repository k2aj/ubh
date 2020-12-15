# UBH

### Building
UBH requires JDK 11 or higher to build and run.

`gradlew build` - creates a runnable fat jar in `./app/build/libs/` directory. \
`gradlew run` - runs the program. \
`gradlew test` - runs unit tests. 

You might need to `chmod u+x gradlew` on Linux. First build will be really slow due to Gradle downloading necessary files.