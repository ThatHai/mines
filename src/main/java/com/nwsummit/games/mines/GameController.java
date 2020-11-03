/*
 * Copyright (c) 2020 - nwsummit.com
 *
 * This software is free under the MIT License (https://www.mit.edu/~amini/LICENSE.md)
 * The software is provided "as is", without warranty of any kind, expressed or implied.
 *
 * Created by: ThatHai on 2020-10-30
 */
package com.nwsummit.games.mines;

import static javafx.scene.paint.Color.*;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * JavaFX controller for the minesweeper game. This is effectively the mediator (pattern)
 * for the UI components and {@link MinesBoard}.
 */
class GameController {

  // number string to display by the Squares; index corresponds to MinesBoard.Cell::value
  private final String[] NUMBS = new String[] {
    " ", "1", "2", "3", "4", "5", "6", "7", "8"
  };

  // colours corresponding to the number strings above
  private final Color[] COLORS = new Color[] {
    BLACK, BLUE, GREEN, RED, DARKBLUE, DARKRED, DARKGREEN, DARKMAGENTA, BLACK
  };

  /**
   * Regex for parsing the game level (rows x columss : mines) input from the UI.
   */
  private final Pattern REGEX_LEVEL = Pattern.compile("(\\d+)x(\\d+):(\\d+)");

  // Unicode symbols for flag and mine
  private final String SYM_FLAG = "\u2691", SYM_MINE = "\u2737";


  @FXML
  private ChoiceBox<String> level;

  @FXML
  private Text txFlags;

  @FXML
  private Text txTime;

  @FXML
  private MinesPane minesPane;

  private MinesBoard minesBoard;

  private int flags, mines;

  private int elapsedTime;
  private Timer timer;

  /**
   * Reference to the primary stage/window. This allows resizing the window when changing
   * the game's rows x columns.
   */
  private final Stage stage;

  GameController(Stage stage) {
    this.stage = stage;
    stage.setOnCloseRequest(event -> stopTimer());
  }

  /**
   * Initializes the mines field for a new game.
   */
  @FXML
  void newGame() {
    stopTimer(); // in case a game is still running

    Matcher matcher = REGEX_LEVEL.matcher(level.getValue());
    if (!matcher.matches())
      throw new IllegalArgumentException("Invalid game level: " + level.getValue());

    // initialize the model
    int rows = Integer.parseInt(matcher.group(1));
    int columns = Integer.parseInt(matcher.group(2));
    mines = Integer.parseInt(matcher.group(3));
    minesBoard = new MinesBoard(rows, columns, mines);
    flags = 0;
    elapsedTime = 0;

    // initialize the UI
    minesPane.initialize(rows, columns, this::handle);
    updateFlags();
    updateElapsedTime();
    stage.sizeToScene();
  }

  /**
   * Updates the game elapsed time in the UI.
   */
  private void updateElapsedTime() {
    int min = elapsedTime / 60;
    int sec = elapsedTime % 60;
    txTime.setText(String.format("\u231B %02d:%02d", min, sec));
  }

  /**
   * Updates the number of flagged squares in the UI.
   */
  private void updateFlags() {
    txFlags.setText(String.format("%s %d/%d", SYM_FLAG, flags, mines));
  }

  private void startTimer() {
    timer = new Timer("MinesGameTimer");
    timer.schedule(new TimerTask() {
	public void run() {
          elapsedTime += 1;
          updateElapsedTime();
        }
      }, 1000L, 1000L);
  }

  private void stopTimer() {
    if (timer != null) {
      timer.cancel();
      timer = null;
    }
  }

  /**
   * Handles the mouse event emitted from the specified square.
   */
  private void handle(MouseEvent event, MinesPane.Square square) {
    if (timer == null && !minesBoard.ended()) {
      startTimer();
    }
    MouseButton button = event.getButton();
    if (button == MouseButton.PRIMARY) {
      updateOpen(square.row(), square.col());
      if (minesBoard.ended()) {
        stopTimer();
        revealMinesField();
      }
    }
    else if (button == MouseButton.SECONDARY) {
      State state = minesBoard.flag(square.row(), square.col());
      if (state == State.FLAGGED) {
        flags += 1;
        square.show(SYM_FLAG, BLACK);
      }
      else if (state == State.UNOPEN) {
        flags -= 1;
        square.show("", BLACK);
      }
      updateFlags();
    }
  }

  /**
   * Updates the UI with opened square(s).
   */
  private void updateOpen(int row, int col) {
    List<MinesBoard.Cell> openedCells = minesBoard.open(row, col);
    for (MinesBoard.Cell cell: openedCells) {
      MinesPane.Square square = minesPane.get(cell.row(), cell.col());
      int value = cell.value();
      if (value == MinesBoard.MINE) {
        square.open(SYM_MINE, RED);
      } else {
        square.open(NUMBS[value], COLORS[value]);
      }
    }
  }

  /**
   * Reveals the mines field at the end of the gam. This includes showing location of
   * unflagged mines, and wrongly flagged mines.
   */
  private void revealMinesField() {
    for (MinesBoard.Cell wrongFlag: minesBoard.getWronglyFlaggedCells()) {
      MinesPane.Square square = minesPane.get(wrongFlag.row(), wrongFlag.col());
      square.show(SYM_FLAG, RED);
    }
    for (MinesBoard.Cell mine: minesBoard.getMines()) {
      if (mine.isUnopen()) {
        MinesPane.Square square = minesPane.get(mine.row(), mine.col());
        square.show(SYM_MINE, BLACK);
      }
    }
  }

  /**
   * Interface for handling {@link MouseEvent} emitted by a {@link MinesPane.Square}.
   */
  interface SquareMouseHandler {
    void handle(MouseEvent event, MinesPane.Square square);
  }
}
