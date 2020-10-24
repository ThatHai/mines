/*
 * Copyright (c) 2020 - nwsummit.com
 *
 * This software is free under the MIT License (https://www.mit.edu/~amini/LICENSE.md)
 * The software is provided "as is", without warranty of any kind, expressed or implied.
 *
 * Created by: ThatHai on 2020-10-20
 */
package com.nwsummit.games.mines;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MinesBoardTest {

  @DataProvider(name = "integers less than 3")
  private Object[] integerValues() {
    return new Object[] {-3, -1, 0, 2};
  }

  @Test(dataProvider = "integers less than 3",
        expectedExceptions = IllegalArgumentException.class)
  public void testInvalidRows(int rows) {
    new MinesBoard(rows, 3, 1);
  }

  @Test(dataProvider = "integers less than 3",
        expectedExceptions = IllegalArgumentException.class)
  public void testInvalidColumns(int columns) {
    new MinesBoard(3, columns, 1);
  }

  @Test
  public void testBoardRandomness() {
    MinesBoard board1 = new MinesBoard(4, 4, 3);
    board1.print(System.out);
    Set<Cell> mines1 = board1.getMines();

    System.out.println();

    MinesBoard board2 = new MinesBoard(4, 4, 3);
    board2.print(System.out);
    Set<Cell> mines2 = board2.getMines();

    // if the placement of mines is really random, then two board of the same size and
    // same mines number would not have the same location for the mines.
    assertEquals(mines1.size(), 3, "Number of mines, board1");
    assertEquals(mines2.size(), 3, "Number of mines, board2");
    assertNotEquals(mines1, mines2, "Mines from board1 and board2");
  }

  @Test
  public void testBoardCorrectness() {
    Random random = new Random(System.currentTimeMillis());
    int rows = 3 + random.nextInt(17);
    int cols = 3 + random.nextInt(17);

    MinesBoard board = new MinesBoard(rows, cols, rows * cols / 4);
    board.print(System.out);

    // our working board
    int[][] expected = new int[rows][cols];

    // place the mines at the same locations in our board
    for (Cell mine: board.getMines()) {
      expected[mine.row()][mine.col()] = -1;
    }

    // with the mines in place, we'll just use the brute force method to compute the
    // number of adjascent mines for all cells of the board. (can't use the same algo
    // as in MinesBoard since we want to test it)
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        // cell is a mine => no counting
        if (expected[r][c] == -1) {
          continue;
        }

        // iterate through the adjascent cell and count the number of mines
        int count = 0;
        for (int i = r-1; i <= r+1; i++) {
          for (int j = c-1; j <= c+1; j++) {
            if (i < 0 || i == rows ||
                j < 0 || j == cols ||
                (i == r && j == c)) {
              continue;
            }
            if (expected[i][j] == -1) {
              count += 1;
            }
          }
        }
        expected[r][c] = count;
      }
    }

    Iterator<Cell> iterator = board.iterator();
    while (iterator.hasNext()) {
      Cell cell = iterator.next();
      assertEquals(cell.value(), expected[cell.row()][cell.col()],
                   "Cell (" + cell.row() + "," + cell.col() + ")");
    }
  }

  @Test
  public void testOpen() {
    HashSet<Cell> mines = new HashSet<>();
    mines.add(new Cell(0, 3, -1));
    mines.add(new Cell(0, 5, -1));
    mines.add(new Cell(1, 6, -1));
    mines.add(new Cell(2, 3, -1));
    mines.add(new Cell(2, 6, -1));
    mines.add(new Cell(5, 0, -1));
    mines.add(new Cell(6, 0, -1));
    mines.add(new Cell(6, 1, -1));
    mines.add(new Cell(6, 5, -1));
    mines.add(new Cell(7, 7, -1));

    /*
      The testing mines board looks like the below:
      (0,0)        columns
        +----------------->
        |  0 0 1 M 2 M 2 1
        |  0 0 2 2 3 3 M 2
        |  0 0 1 M 1 2 M 2
      r |  0 0 1 1 1 1 1 1
      o |  1 1 0 0 0 0 0 0
      w |  M 3 1 0 1 1 1 0
      s |  M M 1 0 1 M 2 1
        v  2 2 1 0 1 1 2 M
    */
    MinesBoard board = new MinesBoard(8, 8, mines);
    board.print(System.out);
    System.out.println();

    assertNull(board.open(6, 0), "Open a mine");
    assertNull(board.open(1, 6), "Open a mine");

    assertEquals(board.open(7, 0), Collections.singletonList(new Cell(7, 0, 2)),
                 "Open cell with 2 adjascent mines");
    assertEquals(board.open(5, 1), Collections.singletonList(new Cell(5, 1, 3)),
                 "Open cell with 3 adjascent mines");
    assertEquals(board.open(5, 6), Collections.singletonList(new Cell(5, 6, 1)),
                 "Open cell with 1 adjascent mine");

    List<Cell> opened = board.open(4, 2);
    // since (5,1) and (5,6) are already opened above, we get 38 instead of 40
    assertEquals(opened.size(), 38, "Opened");
  }
}
