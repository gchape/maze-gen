package com.gen.maze.exec;

import com.gen.maze.Controller;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        stage.setOnCloseRequest((e) -> System.exit(0));
        stage.initStyle(StageStyle.DECORATED);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/appIcon.png")).toExternalForm()));
        stage.setTitle("Perfect Mazes (gchapidze)");
        stage.setResizable(false);

        var scene = new Scene(new Controller().getView());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        stage.setScene(scene);

        stage.show();
    }
}
