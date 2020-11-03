/*
 * Copyright (c) 2020 - nwsummit.com
 *
 * This software is free under the MIT License (https://www.mit.edu/~amini/LICENSE.md)
 * The software is provided "as is", without warranty of any kind, expressed or implied.
 *
 * Created by: ThatHai on 2020-10-31
 */
package com.nwsummit.games.mines;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * A custom {@link GridPane} representing the mines board UI.
 */
// must be public for FXML
public class MinesPane extends GridPane {

  private Square[][] squares;

  /**
   * Initializes this pane to the specified number of (rows, columns) {@link Square}s.
   */
  void initialize(int rows, int columns, GameController.SquareMouseHandler handler) {
    getChildren().clear(); // clear any existing squares

    squares = new Square[rows][columns];

    Paint gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                                        new Stop(0, Color.GREY), new Stop(1, Color.LIGHTGREY));
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        Square square = new Square(r, c, gradient);
        square.setOnMouseClicked(event -> handler.handle(event, square));
        add(square, c, r);
        squares[r][c] = square;
      }
    }
  }

  Square get(int row, int col) {
    return squares[row][col];
  }

  /**
   * UI representation of a square/cell in the mines board.
   */
  class Square extends StackPane {

    private final int row, col;

    private final Text text;

    /**
     * Property controlling the rendering of the square as unopen or open.
     */
    private final SimpleBooleanProperty open = new SimpleBooleanProperty(false);

    Square(int row, int col, Paint gradient) {
      this.row = row;
      this.col = col;

      text = new Text();
      text.setFont(Font.font("serif", FontWeight.BOLD, 16));

      Rectangle background = new Rectangle(30, 30);
      background.setArcWidth(10);
      background.setArcHeight(10);
      background.fillProperty()
        .bind(Bindings.when(open)
                      .then((Paint)Color.BEIGE)
                      .otherwise(gradient));
      getChildren().addAll(background, text);
    }

    int row() {
      return row;
    }

    int col() {
      return col;
    }

    void show(String symb, Color color) {
      text.setText(symb);
      text.setFill(color);
    }

    void open(String symb, Color color) {
      open.set(true);
      show(symb, color);
    }
  }
}
