/*
 * Copyright (c) 2020 - nwsummit.com
 *
 * This software is free under the MIT License (https://www.mit.edu/~amini/LICENSE.md)
 * The software is provided "as is", without warranty of any kind, expressed or implied.
 *
 * Created by: ThatHai on 2020-10-20
 */
package com.nwsummit.games.mines;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;
import com.nwsummit.games.mines.MinesBoard.NeighbourIterator;
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
    Set<MinesBoard.Cell> mines1 = board1.getMines();

    System.out.println();

    MinesBoard board2 = new MinesBoard(4, 4, 3);
    board2.print(System.out);
    Set<MinesBoard.Cell> mines2 = board2.getMines();

    // if the placement of mines is really random, then two board of the same size and
    // same mines number would not have the same location for the mines.
    assertEquals(mines1.size(), 3, "Number of mines, board1");
    assertEquals(mines2.size(), 3, "Number of mines, board2");
    assertNotEquals(mines1, mines2, "Mines from board1 and board2");
  }

  @Test
  public void testNeighbourIterator_CornerCells() {
    MinesBoard board = new MinesBoard(4, 4, 1);
    NeighbourIterator neighbours = (NeighbourIterator)board.neighbourIterator(0, 0);
    assertEquals(neighbours.size(), 3);

    neighbours = (NeighbourIterator)board.neighbourIterator(3, 0);
    assertEquals(neighbours.size(), 3);

    neighbours = (NeighbourIterator)board.neighbourIterator(0, 3);
    assertEquals(neighbours.size(), 3);

    neighbours = (NeighbourIterator)board.neighbourIterator(3, 3);
    assertEquals(neighbours.size(), 3);
  }

  @Test
  public void testNeighbourIterator_EdgeCells() {
    MinesBoard board = new MinesBoard(4, 4, 1);
    NeighbourIterator neighbours = (NeighbourIterator)board.neighbourIterator(0, 2);
    assertEquals(neighbours.size(), 5);

    neighbours = (NeighbourIterator)board.neighbourIterator(1, 0);
    assertEquals(neighbours.size(), 5);

    neighbours = (NeighbourIterator)board.neighbourIterator(3, 2);
    assertEquals(neighbours.size(), 5);

    neighbours = (NeighbourIterator)board.neighbourIterator(2, 3);
    assertEquals(neighbours.size(), 5);
  }

  @Test
  public void testNeighbourIterator_InsideCells() {
    MinesBoard board = new MinesBoard(4, 4, 1);
    NeighbourIterator neighbours = (NeighbourIterator)board.neighbourIterator(1, 2);
    assertEquals(neighbours.size(), 8);
  }

  @Test
  public void testBoardCorrectness() {
    Random random = new Random(System.currentTimeMillis());
    int rows = 3 + random.nextInt(17);
    int cols = 3 + random.nextInt(17);

    MinesBoard board = new MinesBoard(rows, cols, rows * cols / 4);
    board.print(System.out);

    int cellCount = 0;
    Iterator<MinesBoard.Cell> boardIterator = board.iterator();
    while (boardIterator.hasNext()) {
      MinesBoard.Cell cell = boardIterator.next();
      cellCount += 1;
      if (cell.isMine())
        continue;

      int mineCount = 0;
      Iterator<MinesBoard.Cell> neighbourIterator = board.neighbourIterator(cell.row(), cell.col());
      while (neighbourIterator.hasNext()) {
        MinesBoard.Cell neighbour = neighbourIterator.next();
        if (neighbour.isMine()) {
          mineCount += 1;
        }
      }
      assertEquals(cell.value(), mineCount, "Mines count for cell " + cell);
    }
    assertEquals(cellCount, rows * cols, "Number of iterated cells");
  }

  @Test
  public void testOpen_SomeAlreadyOpened() {
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
    MinesBoard board = new MinesBoard(8, 8);
    board.placeMine(0, 3);
    board.placeMine(0, 5);
    board.placeMine(1, 6);
    board.placeMine(2, 3);
    board.placeMine(2, 6);
    board.placeMine(5, 0);
    board.placeMine(6, 0);
    board.placeMine(6, 1);
    board.placeMine(6, 5);
    board.placeMine(7, 7);

    board.print(System.out);
    System.out.println();

    assertEquals(board.open(7, 0), Collections.singleton(board.get(7, 0)));
    assertEquals(board.open(5, 1), Collections.singleton(board.get(5, 1)));
    assertEquals(board.open(5, 6), Collections.singleton(board.get(5, 6)));

    List<MinesBoard.Cell> opened = board.open(4, 2);
    // since (5,1) and (5,6) are already opened above, we get 38 instead of 40
    assertEquals(opened.size(), 38, "Opened");
  }

  /*
    This testing mines board looks like the below:
    (0,0)        columns
      +----------------->
      |  0 1 M 1 0 1 2 2
      |  1 2 1 1 0 1 M M
      |  M 2 1 1 0 1 3 M
    r |  1 2 M 1 0 0 1 1
    o |  0 2 2 3 1 1 0 0
    w |  1 2 M 2 M 1 0 0
    s |  M 2 1 2 2 2 1 0
      v  1 1 0 0 1 M 1 0
  */
  private MinesBoard testingBoard() {
    MinesBoard board = new MinesBoard(8, 8);
    board.placeMine(0, 2);
    board.placeMine(1, 6);
    board.placeMine(1, 7);
    board.placeMine(2, 0);
    board.placeMine(2, 7);
    board.placeMine(3, 2);
    board.placeMine(5, 2);
    board.placeMine(5, 4);
    board.placeMine(6, 0);
    board.placeMine(7, 5);
    return board;
  }

  @Test
  public void testOpen_Mine() {
    MinesBoard board = testingBoard();
    assertNull(board.open(0, 2));
    assertNull(board.open(2, 0));
    assertNull(board.open(7, 5));
    assertNull(board.open(1, 7));
    assertNull(board.open(3, 2));
  }

  @Test
  public void testOpen_SomeFlagged() {
    MinesBoard board = testingBoard();
    List<MinesBoard.Cell> opened = board.open(0, 4);
    assertEquals(opened.size(), 28);

    // same tesing board
    board = testingBoard();
    // this is a square with no adjascent mines
    assertSame(board.flag(3, 5), State.FLAGGED);

    opened = board.open(0, 4);
    assertEquals(opened.size(), 14);
  }

  @Test
  public void testOpen_EmptyCellUnconnected() {
    MinesBoard board = testingBoard();
    assertEquals(Sets.newHashSet(board.open(0, 0)),
                 Sets.newHashSet(board.get(0, 0),
                                 board.get(0, 1),
                                 board.get(1, 0),
                                 board.get(1, 1)));
  }

  @Test
  public void testOpen_AlreadyOpenedCell() {
    MinesBoard board = new MinesBoard(3, 3);
    board.open(0, 0);

    assertTrue(board.open(0, 0).isEmpty());
  }

  @Test
  public void testOpen_FlaggedCell() {
    MinesBoard board = new MinesBoard(3, 3);
    assertEquals(board.flag(0, 0), State.FLAGGED);
    assertTrue(board.open(0, 0).isEmpty());
  }

  @Test
  public void testFlag() {
    MinesBoard board = new MinesBoard(3, 3);
    assertEquals(board.flag(0, 0), State.FLAGGED);
    assertEquals(board.flag(0, 0), State.UNOPENED);

    board.open(0, 0);
    assertEquals(board.flag(0, 0), State.OPENED);
  }
}
