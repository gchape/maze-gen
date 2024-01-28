package com.gen.maze.app;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.function.Consumer;

public class View {
    private final ChoiceBox<Integer> mazeGridDimChoiceBox = new ChoiceBox<>(FXCollections.observableList(List.of(20, 40)));
    private final Button algorithmBtnAldousBroder = new Button("\uD83C\uDF00 Aldous-Broder");
    private final Button algorithmBtnBinaryTree = new Button("\uD83C\uDF33 BinaryTree");
    private final Button algorithmBtnKruskal = new Button("\uD83C\uDF3F Kruskal's");
    private final Button algorithmBtnBacktracking = new Button("➰ Backtracking");
    private final CheckBox animationCheckBox = new CheckBox("On/Off");
    private final Button resetMazeGridBtn = new Button("Reset");
    private final AnchorPane mazeUIPane = new AnchorPane();
    private final VBox userControlsPane = new VBox(15);
    private Canvas canvas = new Canvas(800, 800);
    private final VBox algorithmsVBox = new VBox(8);
    private final GridPane root = new GridPane();

    public View() {
        mazeGridDimChoiceBox.setValue(20);

        initAlgorithmsVBox();

        root.addColumn(0, mazeUIPane);
        root.addColumn(1, userControlsPane);

        userControlsPane.getChildren().addAll(new VBox(3, new Label("[Choose grid size \uD83D\uDCAA]"), mazeGridDimChoiceBox), new VBox(3, new Label("[Animate algs \uD83D\uDD27]"), animationCheckBox), new VBox(3, new Label("[Reset maze \uD83D\uDD19]"), resetMazeGridBtn), algorithmsVBox);
    }

    public GraphicsContext graphics() {
        return canvas.getGraphicsContext2D();
    }

    protected AnchorPane getMazeUIPane() {
        return mazeUIPane;
    }

    protected GridPane getRoot() {
        return root;
    }

    public Canvas getCanvas() {
        mazeUIPane.getChildren().remove(canvas);
        canvas = new Canvas(canvas.getWidth(), canvas.getHeight());
        canvas.getGraphicsContext2D().setLineWidth(3);
        canvas.getGraphicsContext2D().setStroke(Color.WHITE);

        return canvas;
    }

    private void initAlgorithmsVBox() {
        algorithmBtnKruskal.setId("kruskal");
        algorithmBtnBinaryTree.setId("binaryTree");
        algorithmBtnBacktracking.setId("backtracking");
        algorithmBtnAldousBroder.setId("aldous-broder");
        // New Algorithm Button

        algorithmsVBox.getStyleClass().add("algorithms-vbox");

        algorithmsVBox.getChildren().addAll(new Label("[Choose maze algs \uD83D\uDE80]"), algorithmBtnAldousBroder, algorithmBtnBacktracking, algorithmBtnBinaryTree, algorithmBtnKruskal); // New Algorithm Button
    }

    protected void setOnBtnAlgorithmsClicked(Consumer<MouseEvent> eventTask) {
        algorithmBtnAldousBroder.setOnMouseClicked(eventTask::accept);
        algorithmBtnBacktracking.setOnMouseClicked(eventTask::accept);
        algorithmBtnBinaryTree.setOnMouseClicked(eventTask::accept);
        algorithmBtnKruskal.setOnMouseClicked(eventTask::accept);
        // New Algorithm Button Event
    }

    protected ObjectProperty<Integer> choiceBoxValueProperty() {
        return mazeGridDimChoiceBox.valueProperty();
    }

    protected BooleanProperty mazeGridResetBtnDisableProperty() {
        return resetMazeGridBtn.disableProperty();
    }

    protected BooleanProperty choiceBoxDisableProperty() {
        return mazeGridDimChoiceBox.disableProperty();
    }

    protected BooleanProperty algorithmsVBoxDisableProperty() {
        return algorithmsVBox.disableProperty();
    }

    protected BooleanProperty animationCheckedProperty() {
        return animationCheckBox.selectedProperty();
    }

    protected BooleanProperty animationDisableProperty() {
        return animationCheckBox.disableProperty();
    }

    protected void setOnBtnResetMazeGridClicked(Consumer<MouseEvent> eventTask) {
        resetMazeGridBtn.setOnMouseClicked(eventTask::accept);
    }
}
