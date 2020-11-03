# mines: An implementation of Minesweeper

This is an implementation of the minesweeper game in Java. Its UI is implemented using [JavaFX](https://openjfx.io/).

## Running Mines
JDK 11 or later is required for this project. You can run the game from root directory of this gradle project as follows:

```console
$ ./gradlew run
```
Or you can create an install distribution (see Building section below) and run the app with the script for your platform in the `bin` directory, that is `bin/mines` on Linux and `bin\mines.bat` on Windows.

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
