/*
 * Copyright (c) 2020 - nwsummit.com
 *
 * This software is free under the MIT License (https://www.mit.edu/~amini/LICENSE.md)
 * The software is provided "as is", without warranty of any kind, expressed or implied.
 *
 * Created by: ThatHai on 2020-10-21
 */
package com.nwsummit.games.mines;

import java.util.Objects;

/**
 * Represents a cell, or square, in a {@link MinesBoard}. The cell's location on the
 * board is given by its {@link #row} and {@link #col}. Its {@link #value} indicates
 * whether it's a mine (-1), or the number of mines that the cell is adjascent to.
 */
class Cell {
  private final int row;
  private final int col;
  private int value;
  private State state;

  Cell(int row, int col, int value) {
    this.row = row;
    this.col = col;
    this.value = value;
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

  State state() {
    return state;
  }

  void setState(State state) {
    this.state = state;
  }

  int value() {
    return value;
  }

  void incrementValue() {
    value += 1;
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
