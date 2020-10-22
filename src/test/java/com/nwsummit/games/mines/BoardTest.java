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

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class BoardTest {

  @DataProvider(name = "integers less than 3")
  private Object[] integerValues() {
    return new Object[] {-3, -1, 0, 2};
  }

  @Test(dataProvider = "integers less than 3",
        expectedExceptions = IllegalArgumentException.class)
  public void testInvalidRows(int rows) {
    new Board(rows, 3, 1);
  }

  @Test(dataProvider = "integers less than 3",
        expectedExceptions = IllegalArgumentException.class)
  public void testInvalidColumns(int columns) {
    new Board(3, columns, 1);
  }

  @Test
  public void testBoardRandomness() {
    Board board1 = new Board(4, 4, 3);
    board1.print(System.out);
    List<Cell> mines1 = board1.getMines();

    System.out.println();

    Board board2 = new Board(4, 4, 3);
    board2.print(System.out);
    List<Cell> mines2 = board2.getMines();

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

    Board board = new Board(rows, cols, rows * cols / 4);
    board.print(System.out);

    // our working board
    int[][] expected = new int[16][16];

    // place the mines at the same locations in our board
    for (Cell mine: board.getMines()) {
      expected[mine.row()][mine.col()] = -1;
    }

    // with the mines in place, we'll just use the brute force method to compute the
    // number of adjascent mines for all cells of the board. (can't use the same algo
    // as in Board since we want to test it)
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        // skip the cell is a mine => no counting
        if (expected[r][c] == -1) {
          continue;
        }

        // iterate through the adjascing cell and count the number of mines
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

    System.out.println("expected:");
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        Object obj = expected[r][c] == -1 ? "M" : expected[r][c];
        System.out.printf(" %s", obj);
      }
      System.out.println();
    }

    Iterator<Cell> iterator = board.iterator();
    while (iterator.hasNext()) {
      Cell cell = iterator.next();
      assertEquals(cell.value(), expected[cell.row()][cell.col()],
                   "Cell (" + cell.row() + "," + cell.col() + ")");
    }
  }
}
