/*
 * Copyright (c) 2020 - nwsummit.com
 *
 * This software is free under the MIT License (https://www.mit.edu/~amini/LICENSE.md)
 * The software is provided "as is", without warranty of any kind, expressed or implied.
 *
 * Created by: ThatHai on 2020-10-23
 */
package com.nwsummit.games.mines;

/**
 * State of a {@link MinesBoard.Cell}.
 */
enum State {
  FLAGGED,
  OPEN,
  UNOPEN
}
