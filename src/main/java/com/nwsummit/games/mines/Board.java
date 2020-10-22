/*
 * Copyright (c) 2020 - nwsummit.com
 *
 * This software is free under the MIT License (https://www.mit.edu/~amini/LICENSE.md)
 * The software is provided "as is", without warranty of any kind, expressed or implied.
 *
 * Created by: ThatHai on 2020-10-20
 */
package com.nwsummit.games.mines;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * Class representing a mines board. A mines board is characterised by
 * <ul>
 * <li>the number of rows and columns, or size of the board.</li>
 * <li>the number of mines.</li>
 * </ul>
 */
class Board implements Iterable<Cell> {
  private static final Random RANDOM = new Random(System.currentTimeMillis());

  private final int[][] board;
  private final int rows, columns, mines;

  /**
   * Construct a mines board of the specified size and number of mines.
   * @param rows number of rows.
   * @param columns number of columns.
   * @param mines number of mines.
   */
  Board(int rows, int columns, int mines) {
    checkArgument(rows > 2, "Rows must be greater than 2");
    checkArgument(columns > 2, "Columns must be greater than 2");
    checkArgument(mines > 0 && mines <= (rows * columns / 3),
                  "There must be at least one, and not too many mines");

    this.rows = rows;
    this.columns = columns;
    this.mines = mines;

    board = new int[rows][columns];
    placeMines();
  }

  private void placeMines() {
    int i = 0;
    do {
      // randomly pick a spot on the board
      int r = RANDOM.nextInt(rows);
      int c = RANDOM.nextInt(columns);

      // place a mine there if not already placed
      if (board[r][c] != -1) {
	board[r][c] = -1;
        updateMinesCount(r, c);
        i += 1;
      }
    } while (i < mines);
  }

  /**
   * Given the location of a mine, updates the adjascent mines count of the surrounding
   * cells. The mine location is given by the input (row, col).
   */
  private void updateMinesCount(int row, int col) {
    for (int r = row-1; r <= row+1; r++) {
      for (int c = col-1; c <= col+1; c++) {
        // make sure that (r,c) is within the board and it's not a mine cell
        if (0 <= r && r < rows &&
            0 <= c && c < columns &&
            board[r][c] != -1) {
          // update the number of adjascent mine for the cell
          board[r][c] += 1;
        }
      }
    }
  }

  @Override
  public java.util.Iterator<Cell> iterator() {
    return new Iterator();
  }

  /**
   * Returns the list of mines of this board.
   */
  List<Cell> getMines() {
    LinkedList<Cell> mines = new LinkedList<>();
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        if (board[r][c] == -1) {
          mines.add(newCell(r, c));
        }
      }
    }
    return mines;
  }

  void print(PrintStream stream) {
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        Object obj = board[r][c] == -1 ? "M" : board[r][c];
        stream.printf(" %s", obj);
      }
      stream.println();
    }
  }

  private Cell newCell(int row, int col) {
    return new Cell(row, col, board[row][col]);
  }

  /**
   * Class for iterating through the {@link Board} row by row. i.e. it iterates through the
   * cells of the first row, then of the second row, etc.
   */
  class Iterator implements java.util.Iterator<Cell> {
    private int curRow, curCol;

    private Iterator() {
      curRow = curCol = 0;
    }

    @Override
    public boolean hasNext() {
      return curRow < Board.this.rows;
    }

    @Override
    public Cell next() {
      if (curRow >= Board.this.rows) {
        throw new NoSuchElementException("No more Board.Cell to iterate");
      }
      Cell cell = newCell(curRow, curCol);
      if (curCol < Board.this.columns) {
        if (curCol == Board.this.columns - 1) {
          // reaching the end of a row, move to next row
          curCol = 0;
          curRow += 1;
        } else {
          curCol += 1;
        }
      }
      return cell;
    }
  }
}
