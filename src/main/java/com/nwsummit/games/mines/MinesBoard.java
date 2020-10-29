/*
 * Copyright (c) 2020 - nwsummit.com
 *
 * This software is free under the MIT License (https://www.mit.edu/~amini/LICENSE.md)
 * The software is provided "as is", without warranty of any kind, expressed or implied.
 *
 * Created by: ThatHai on 2020-10-20
 */
package com.nwsummit.games.mines;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Class representing a mines board. A mines board is characterised by
 * <ul>
 * <li>the number of rows and columns, or size of the board.</li>
 * <li>the number of mines.</li>
 * </ul>
 * Each square on the board is represented by a {@link Cell} and the board can be iterated
 * via an {@link #iterator}.
 * @see Cell
 */
class MinesBoard implements Iterable<MinesBoard.Cell> {

  /**
   * Value representing a mine.
   */
  static final int MINE = -1;

  // number of rows and columns
  private final int rows, columns;

  private final Cell[][] board;

  private Set<Cell> mines;

  /**
   * The number of remaining unopen/unresolved cells.
   */
  private int unopen;

  private boolean kaboom;

  // for testing
  MinesBoard(int rows, int columns) {
    checkArgument(rows > 2, "Rows must be greater than 2");
    checkArgument(columns > 2, "Columns must be greater than 2");

    this.rows = rows;
    this.columns = columns;
    this.mines = new HashSet<>();
    this.board = new Cell[rows][columns];

    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        board[r][c] = new Cell(r, c);
      }
    }
  }

  /**
   * Construct a mines board of the specified size, with the specifed number of mines.
   *
   * @param rows number of rows.
   * @param columns number of columns.
   * @param mines number of mines.
   */
  public MinesBoard(int rows, int columns, int mines) {
    this(rows, columns);

    int maxCells = rows * columns;
    checkArgument(mines > 0 && mines < maxCells,
                  "Invalid 0 < mines=%d < (rows x colums)=%d", mines, maxCells);
    placeMines(mines);
    unopen = maxCells - mines;
  }

  /**
   * Randomly places the specified number of mines on the board.
   * @param nMines the number of mines to be placed.
   */
  private void placeMines(int nMines) {
    Random random = new Random(System.nanoTime());
    do {
      int r = random.nextInt(rows);
      int c = random.nextInt(columns);
      if (!board[r][c].isMine())
        placeMine(r, c);
    } while (mines.size() < nMines);
  }

  /**
   * Places a mine at the specified (row, col) and updates the mines count of the cells
   * adjascent to the mine.
   */
  // visible for testing
  void placeMine(int row, int col) {
    Cell mine = board[row][col];
    mine.value = MINE;
    mines.add(mine);

    // update the mines count of the cells adjascent to the mine
    for (Cell adjCell: neighboursOf(mine, Predicate.not(Cell::isMine))) {
      adjCell.value += 1;
    }
  }

  // visible for testing
  public Cell get(int row, int col) {
    validate(row, col);
    return board[row][col];
  }

  /**
   * Flags the specified cell/square of the board as having a mine.
   */
  public State flag(int row, int col) {
    validate(row, col);
    return board[row][col].flag();
  }

  /**
   * Opens the specified cell/square of the mines board. If a mine is opened in the
   * process, usually be cause the specified cell to open is a mine or some squares
   * were wrongly flagged as mine, {@link #kaboom} will be set to true.
   *
   * @see #getWronglyFlaggedCells
   * @return the list of mines opened; empty if the cell is flagged or already open.
   */
  public List<Cell> open(int row, int col) {
    validate(row, col);

    Cell cell = board[row][col];
    // nothing to open if flagged or already opened but not having full count flags
    if (cell.state == State.FLAGGED || (cell.state == State.OPEN && !isFullyFlagged(cell))) {
      return Collections.emptyList();
    }

    // predicate to selct UNOPEN and not FLAGGED cells
    Predicate<Cell> unopenAndNotFlagged = Predicate.not(Cell::isFlagged).and(Cell::isUnopen);

    LinkedList<Cell> openedCells = new LinkedList<>();

    // cells opening is like breadth frist search in a graph, with cells being vertices,
    // hence a queue is used to store the (adjascent) cells to work on at next iteration
    LinkedList<Cell> queue = new LinkedList<>();
    if (cell.state == State.UNOPEN) {
      queue.add(cell);
    } else {
      // already OPENED then it must be fully flagged, => start by opening its UNOPENED
      // neighours because the algo doesn't open neighbours of a numbered cell
      queue.addAll(neighboursOf(cell, unopenAndNotFlagged));
    }

    // 1. if the cell to open is numbered (value 1..8), then cells opening stops there
    // 2. if the cell to open is not adjascent to any mines, then open its neighbouring cells
    // 3. repeat 1 & 2 until done
    while (!queue.isEmpty()) {
      cell = queue.poll();
      // might have been opened since last queued
      if (cell.isOpen()) {
        continue;
      }
      cell.state = State.OPEN;
      openedCells.add(cell);
      unopen -= 1;
      if (cell.isMine()) {
        kaboom = true;
        break; // game over, no need to open more
      }

      if (cell.value() == 0) {
        queue.addAll(neighboursOf(cell, unopenAndNotFlagged)); // to work on next
      }
    }
    return openedCells;
  }

  /**
   * Returns the cells wrongly flagged as being a mine.
   */
  public List<Cell> getWronglyFlaggedCells() {
    LinkedList<Cell> list = new LinkedList<>();
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        if (board[r][c].isFlagged() && !board[r][c].isMine())
          list.add(board[r][c]);
      }
    }
    return list;
  }

  /**
   * Returns true if a mine has been opened, i.e. game over; false otherwise.
   */
  public boolean kaboom() {
    return kaboom;
  }

  /**
   * Whether all the mines have been swept.
   */
  public boolean isSwept() {
    if (unopen < 0)
      throw new IllegalStateException("Unopen count: " + unopen);
    return unopen == 0;
  }

  // visible for testing
  boolean isFullyFlagged(Cell cell) {
    int flagCount = neighboursOf(cell, Cell::isFlagged).size();
    return flagCount >= cell.value;
  }

  private void checkArgument(boolean condition, String message, Object ... args) {
    if (!condition) {
      throw new IllegalArgumentException(String.format(message, args));
    }
  }

  private void validate(int row, int col) throws IllegalArgumentException {
    checkArgument(0 <= row && row < rows, "Row out of bound: %d", row);
    checkArgument(0 <= col && col < columns, "Col out of bound: %d", col);
  }

  @Override
  public Iterator<Cell> iterator() {
    return new BoardIterator();
  }

  List<Cell> neighboursOf(int row, int col) {
    validate(row, col);
    return neighboursOf(board[row][col], (cell) -> true);
  }

  /**
   * Returns the cells adjascent to the specified cell.
   */
  private List<Cell> neighboursOf(Cell cell, Predicate<Cell> predicate) {
    LinkedList<Cell> neighbours = new LinkedList<>();
    int rmax = Math.min(cell.row() + 1, MinesBoard.this.rows -1);
    int cmax = Math.min(cell.col() + 1, MinesBoard.this.columns -1);
    for (int r = Math.max(cell.row() - 1, 0); r <= rmax; r++) {
      for (int c = Math.max(cell.col() - 1, 0); c <= cmax; c++) {
        if (predicate.test(board[r][c])) {
          neighbours.add(board[r][c]);
        }
      }
    }
    neighbours.remove(cell);
    return neighbours;
  }

  /**
   * Returns the mines on this board.
   */
  Set<Cell> getMines() {
    return Collections.unmodifiableSet(mines);
  }

  void print(PrintStream stream) {
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        Object obj = board[r][c].isMine() ? "M" : board[r][c].value();
        stream.printf(" %s", obj);
      }
      stream.println();
    }
  }

  /**
   * Class for iterating through the {@link MinesBoard} row by row. i.e. it iterates through the
   * cells of the first row, then of the second row, etc.
   */
  class BoardIterator implements Iterator<Cell> {
    private int curRow, curCol;

    private BoardIterator() {
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

  /**
   * Represents a cell/square on the mines board. A cell has a {@link #value} that is either
   * a MINE, or the number of mines surrounding the cell.
   */
  class Cell {
    private final int row;
    private final int col;
    private int value;
    private State state;

    Cell(int row, int col) {
      this.row = row;
      this.col = col;
      this.value = 0;
      this.state = State.UNOPEN;
    }

    /**
     * The mines board's row (zero-base) where this cell is.
     */
    int row() {
      return row;
    }

    /**
     * The mines board's column (zero-base) where this cell is.
     */
    int col() {
      return col;
    }

    int value() {
      return value;
    }

    boolean isMine() {
      return value == MINE;
    }

    boolean isOpen() {
      return state == State.OPEN;
    }

    boolean isUnopen() {
      return state == State.UNOPEN;
    }

    boolean isFlagged() {
      return state == State.FLAGGED;
    }

    /**
     * Flags the cell as being a mine. If the cell is already open, it's a no-op.
     * If the cell was flagged, this toggles off the flag, i.e. back to unopen.
     *
     * @return the resulting state of the cell.
     */
    State flag() {
      if (state == State.OPEN) {
        return State.OPEN;
      }
      state = (state == State.UNOPEN) ? State.FLAGGED : State.UNOPEN;
      return state;
    }

    @Override
    public int hashCode() {
      return Objects.hash(row, col, value);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null || getClass() != obj.getClass()) return false;

      Cell that = (Cell) obj;
      return this.row == that.row
        && this.col == that.col
        && this.value == that.value;
    }

    @Override
    public String toString() {
      return "Cell[" + row + "," + col + "," + value + "]";
    }
  }
}
