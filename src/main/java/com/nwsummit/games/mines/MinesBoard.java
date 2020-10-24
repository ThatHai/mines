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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

/**
 * Class representing a mines board. A mines board is characterised by
 * <ul>
 * <li>the number of rows and columns, or size of the board.</li>
 * <li>the number of mines.</li>
 * </ul>
 */
class MinesBoard implements Iterable<Cell> {
  private static final Random RANDOM = new Random(System.currentTimeMillis());

  private static final int MINE = -1;

  private Cell[][] board;

  // number of rows and columns
  private final int rows, columns;

  private final Set<Cell> mines = new HashSet<>();

  /**
   * Construct a mines board of the specified size, with the specifed number of mines.
   * @param rows number of rows.
   * @param columns number of columns.
   * @param mines number of mines.
   */
  MinesBoard(int rows, int columns, int mines) {
    checkArgument(rows > 2, "Rows must be greater than 2");
    checkArgument(columns > 2, "Columns must be greater than 2");
    checkArgument(mines > 0 && mines <= (rows * columns / 3),
                  "There must be at least one, and not too many mines");

    this.rows = rows;
    this.columns = columns;

    createBoard();
    placeMines(mines);
  }

  /**
   * Creates a mines board of the specified size, with the specified mines.
   */
  // for testing
  MinesBoard(int rows, int columns, Set<Cell> mines) {
    this.rows = rows;
    this.columns = columns;
    this.mines.addAll(mines);

    createBoard();
    for (Cell mine : mines) {
      board[mine.row()][mine.col()] = mine;
      updateMinesCount(mine);
    }
  }

  private void createBoard() {
    board = new Cell[rows][columns];
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        board[r][c] = new Cell(r, c, 0);
      }
    }
  }

  private void placeMines(int nbMines) {
    int i = 0;
    do {
      // randomly pick a spot on the board
      int r = RANDOM.nextInt(rows);
      int c = RANDOM.nextInt(columns);

      // place a mine there if not already placed
      if (board[r][c].value() != MINE) {
	Cell mine = board[r][c] = new Cell(r, c, MINE);
        mines.add(mine);
        updateMinesCount(mine);
        i += 1;
      }
    } while (i < nbMines);
  }

  /**
   * Given the mine, updates the adjascent mines count of its surrounding cells.
   */
  private void updateMinesCount(Cell mine) {
    int row = mine.row();
    int col = mine.col();
    for (int r = row-1; r <= row+1; r++) {
      for (int c = col-1; c <= col+1; c++) {
        // make sure that (r,c) is within the board and it's not a mine cell
        if (0 <= r && r < rows && 0 <= c && c < columns &&
            board[r][c].value() != MINE) {
          board[r][c].incrementValue(); // increment mines count
        }
      }
    }
  }

  /**
   * Opens the specified cell of the mines board.
   *
   * @return null if the cell is a mine; a non empty collection of opened cells otherwise.
   */
  public List<Cell> open(int row, int col) {
    checkArgument(0 <= row && row < rows, "Row is out of bound: %d", row);
    checkArgument(0 <= col && col < columns, "Col is out of bound: %d", col);

    Cell cell = board[row][col];
    cell.setState(State.OPENED);

    // opened cell is a mine
    if (cell.value() == MINE) {
      return null;
    }

    // nothing else to open if the cell is adjascent to one or more mines
    if (cell.value() > 0) {
      return Collections.singletonList(cell);
    }

    // if the opened cell is numbered (value 1..8), then cell opening stops there
    // if the opened cell is not adjascent to any mines, then open its neighbouring cells
    //
    // cells opening can be seen as a breadth frist search in a graph, with cells being vertices.
    LinkedList<Cell> opened = new LinkedList<>();
    opened.add(cell);

    // queue is used to store the (adjascent) cells to work on at next iteration
    LinkedList<Cell> queue = new LinkedList<>();
    queue.add(cell);

    while (!queue.isEmpty()) {
      cell = queue.poll();
      if (cell.value() == 0) {
        int cr = cell.row();
        int cc = cell.col();
        // iterate the cell's adjascent cells and open them
        for (int r = cr-1; r <= cr+1; r++) {
          for (int c = cc-1; c <= cc+1; c++) {
            if (0 <= r && r < rows && 0 <= c && c < columns) {
              Cell adjCell = board[r][c];
              // add to the queue if it hasn't been previously opened
              if (adjCell.state() == State.UNOPENED) {
                opened.add(adjCell);
                queue.add(adjCell);
                adjCell.setState(State.OPENED);
              }
            }
          }
        }
      }
    }
    return opened;
  }

  @Override
  public java.util.Iterator<Cell> iterator() {
    return new Iterator();
  }

  /**
   * Returns this board's collection of mines.
   */
  Set<Cell> getMines() {
    return Collections.unmodifiableSet(mines);
  }

  void print(PrintStream stream) {
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        Object obj = board[r][c].value() == MINE ? "M" : board[r][c].value();
        stream.printf(" %s", obj);
      }
      stream.println();
    }
  }

  /**
   * Class for iterating through the {@link MinesBoard} row by row. i.e. it iterates through the
   * cells of the first row, then of the second row, etc.
   */
  class Iterator implements java.util.Iterator<Cell> {
    private int curRow, curCol;

    private Iterator() {
      curRow = curCol = 0;
    }

    @Override
    public boolean hasNext() {
      return curRow < MinesBoard.this.rows;
    }

    @Override
    public Cell next() {
      if (curRow >= MinesBoard.this.rows) {
        throw new NoSuchElementException("No more Board.Cell to iterate");
      }
      Cell cell = board[curRow][curCol];
      if (curCol < MinesBoard.this.columns) {
        if (curCol == MinesBoard.this.columns - 1) {
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
