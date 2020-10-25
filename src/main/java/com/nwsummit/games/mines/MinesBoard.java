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

/**
 * Class representing a mines board. A mines board is characterised by
 * <ul>
 * <li>the number of rows and columns, or size of the board.</li>
 * <li>the number of mines.</li>
 * </ul>
 */
class MinesBoard implements Iterable<MinesBoard.Cell> {
  /**
   * Value representing a mine.
   */
  static final int MINE = -1;

  private static final Random RANDOM = new Random(System.currentTimeMillis());

  // number of rows and columns
  private final int rows, columns;

  private final Cell[][] board;

  private Set<Cell> mines;

  // for testing
  MinesBoard(int rows, int columns) {
    checkArgument(rows > 2, "Rows must be greater than 2");
    checkArgument(columns > 2, "Columns must be greater than 2");

    this.rows = rows;
    this.columns = columns;
    this.mines = new HashSet<>();
    this.board = new Cell[rows][columns];
    initialize();
  }

  /**
   * Construct a mines board of the specified size, with the specifed number of mines.
   *
   * @param rows number of rows.
   * @param columns number of columns.
   * @param mines number of mines.
   */
  MinesBoard(int rows, int columns, int mines) {
    this(rows, columns);
    placeMines(mines);
  }

  /**
   * Initialize the whole board with no mine.
   */
  private void initialize() {
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        board[r][c] = new Cell(r, c);
      }
    }
  }

  /**
   * Randomly places the specified number of mines on the board.
   * @param nMines the number of mines to be placed.
   */
  private void placeMines(int nMines) {
    checkArgument(nMines > 0 && nMines <= (rows * columns), "Invalid number of mines: %d", nMines);
    mines.clear();
    do {
      // randomly pick a spot on the board
      int r = RANDOM.nextInt(rows);
      int c = RANDOM.nextInt(columns);
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
    validate(row, col);

    Cell mine = board[row][col];
    mine.value = MINE;
    mines.add(mine);

    // update the mines count of the cells adjascent to the mine
    Iterator<Cell> neighbours = neighbourIterator(mine);
    while (neighbours.hasNext()) {
      Cell adjCell = neighbours.next();
      if (!adjCell.isMine())
        adjCell.incrementValue();
    }
  }

  // visible for testing
  Cell get(int row, int col) {
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
   * Opens the specified cell/square of the mines board.
   *
   * @return null if the cell is a mine;
             a collection, possibly empty, of opened cells otherwise.
   */
  public List<Cell> open(int row, int col) {
    validate(row, col);

    Cell cell = board[row][col];
    // nothing to open if already opened of flagged
    if (cell.state == State.OPENED || cell.state == State.FLAGGED) {
      return Collections.emptyList();
    }

    cell.state = State.OPENED;

    if (cell.isMine()) {
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
    LinkedList<Cell> openedCells = new LinkedList<>();
    openedCells.add(cell);

    // queue is used to store the (adjascent) cells to work on at next iteration
    LinkedList<Cell> queue = new LinkedList<>();
    queue.add(cell);

    while (!queue.isEmpty()) {
      cell = queue.poll();
      if (cell.value() == 0) {
        Iterator<Cell> neighbours = neighbourIterator(cell);
        while (neighbours.hasNext()) {
          Cell neighbour = neighbours.next();
          if (neighbour.state == State.UNOPENED) {
            neighbour.state = State.OPENED;
            openedCells.add(neighbour);
            queue.add(neighbour); // to work on at next open iteration
          }
        }
      }
    }
    return openedCells;
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

  Iterator<Cell> neighbourIterator(int row, int col) {
    validate(row, col);
    return neighbourIterator(board[row][col]);
  }

  private Iterator<Cell> neighbourIterator(Cell cell) {
    return new NeighbourIterator(cell);
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

  class NeighbourIterator implements Iterator<Cell> {
    private Cell[] neighbours;
    private int i = 0;

    NeighbourIterator(Cell cell) {
      LinkedList<Cell> neighbourList = new LinkedList<>();
      int rmax = Math.min(cell.row() + 1, MinesBoard.this.rows -1);
      int cmax = Math.min(cell.col() + 1, MinesBoard.this.columns -1);
      for (int r = Math.max(cell.row() - 1, 0); r <= rmax; r++) {
        for (int c = Math.max(cell.col() - 1, 0); c <= cmax; c++) {
          neighbourList.add(board[r][c]);
        }
      }
      neighbourList.remove(cell);
      neighbours = neighbourList.toArray(new Cell[neighbourList.size()]);
    }

    @Override
    public boolean hasNext() {
      return i < neighbours.length;
    }

    @Override
    public Cell next() {
      if (i >= neighbours.length) {
        throw new NoSuchElementException();
      }
      return neighbours[i++];
    }

    int size() {
      return neighbours.length;
    }
  }

  class Cell {
    private final int row;
    private final int col;
    private int value;
    private State state;

    Cell(int row, int col) {
      this.row = row;
      this.col = col;
      this.value = 0;
      this.state = State.UNOPENED;
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

    private void incrementValue() {
      value += 1;
    }

    boolean isMine() {
      return value == MINE;
    }

    State state() {
      return state;
    }

    State flag() {
      if (state == State.OPENED)
        return State.OPENED;
      state = (state == State.UNOPENED) ? State.FLAGGED : State.UNOPENED;
      return state;
    }

    @Override
    public int hashCode() {
      return Objects.hash(row, col, value);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null || getClass() != obj.getClass())
        return false;

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
