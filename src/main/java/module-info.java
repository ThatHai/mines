module games.mines {
  requires javafx.controls;
  requires javafx.fxml;

  // for fxml to create MinesPane
  opens com.nwsummit.games.mines to javafx.fxml;

  exports com.nwsummit.games.mines;
}
