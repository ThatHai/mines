# mines: An implementation of Minesweeper

This is an implementation of the minesweeper game in Java. Its UI is implemented using [JavaFX](https://openjfx.io/).

[JDK 11](https://openjdk.java.net/projects/jdk/) or later is required for this project.

## Running Mines
The easiest way to run the game is using the Gradle build. After checking out this  project, you can run the game from its root directory as follows:

```console
$ git clone https://github.com/ThatHai/mines.git
$ cd mines
$ ./gradlew run
```
Instead of cloning the project, you can just download the project zip file.

Alternatively, you can create a distribution of the project and run the game with the generated script:
```console
$ ./gradlew installDist
$ ./build/install/mines/bin/mines
```

Note: JavaFX runtime libs are platform specific, thus you can only run the game on the platform you create the distribution for.

## Building
This is an application gradle project, thus the usual gradle tasks apply:

to build
```console
$ ./gradlew build
```
to run unit tests
```console
$ ./gradlew test
```
to create the install distribution (located in `build/install`)
```console
$ ./gradlew installDist
```
