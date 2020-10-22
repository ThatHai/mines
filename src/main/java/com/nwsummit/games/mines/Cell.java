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
 * Represents a cell, or square, in the mines board.
 */
class Cell {
  int row, col, value;

  Cell(int row, int col, int value) {
    this.row = row;
    this.col = col;
    this.value = value;
  }

  int row() {
    return row;
  }

  int col() {
    return col;
  }

  int value() {
    return value;
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
}
