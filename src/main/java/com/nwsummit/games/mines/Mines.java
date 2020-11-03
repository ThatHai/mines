/*
 * Copyright (c) 2020 - nwsummit.com
 *
 * This software is free under the MIT License (https://www.mit.edu/~amini/LICENSE.md)
 * The software is provided "as is", without warranty of any kind, expressed or implied.
 *
 * Created by: ThatHai on 2020-10-29
 */
package com.nwsummit.games.mines;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * A minesweeper game.
 */
public class Mines extends Application {

  @Override
  public void start(Stage stage) throws IOException {
    GameController controller = new GameController(stage);

    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(getClass().getResource("mines.fxml"));
    loader.setController(controller);

    BorderPane pane = loader.<BorderPane>load();

    Scene scene = new Scene(pane);
    stage.setScene(scene);
    stage.setTitle("Mines");
    stage.show();

    controller.newGame();
  }

  public static void main(String... args) {
    launch(args);
  }
}
