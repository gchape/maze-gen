package com.gen.maze.app;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

public class View {
    private final AnchorPane mazeUIPane = new AnchorPane();
    private final VBox userControlsPane = new VBox();
    private final VBox algorithmsVBox = new VBox();
    private final GridPane root = new GridPane();
    private final ChoiceBox<Integer> mazeGridDimChoiceBox = new ChoiceBox<>(FXCollections.observableList(List.of(20, 40)));
    private final Button algorithmBtnBinaryTree = new Button("\uD83C\uDF33 BinaryTree");
    private final Button algorithmBtnKruskal = new Button("\uD83C\uDF3F Kruskal's");
    private final Button algorithmBtnBacktracking = new Button("➰ Backtracking");
    private final CheckBox animationCheckBox = new CheckBox("On/Off");

    public View() {
        userControlsPane.getStyleClass().add("vbox");
        mazeGridDimChoiceBox.setValue(20);

        initAlgorithmsVBox();

        root.addColumn(0, mazeUIPane);
        root.addColumn(1, userControlsPane);
        userControlsPane.getChildren().addAll(new Label("[Choose grid size \uD83D\uDCAA]"), mazeGridDimChoiceBox, new Label("[Animate algs \uD83D\uDD27]"), animationCheckBox, algorithmsVBox);
    }

    private void initAlgorithmsVBox() {
        algorithmBtnKruskal.setId("kruskal");
        algorithmBtnBinaryTree.setId("binaryTree");
        algorithmBtnBacktracking.setId("backtracking");
        // New Algorithm Button

        algorithmsVBox.getStyleClass().add("algorithms-vbox");

        algorithmsVBox.getChildren().addAll(new Label("[Choose maze algs \uD83D\uDE80]"), algorithmBtnBacktracking, algorithmBtnBinaryTree, algorithmBtnKruskal); // New Algorithm Button
    }

    protected void setOnButtonClicked(Consumer<MouseEvent> buttonConsumer) {
        algorithmBtnBacktracking.setOnMouseClicked(buttonConsumer::accept);
        algorithmBtnBinaryTree.setOnMouseClicked(buttonConsumer::accept);
        algorithmBtnKruskal.setOnMouseClicked(buttonConsumer::accept);
        // New Algorithm Button Event
    }

    protected AnchorPane getMazeUIPane() {
        return mazeUIPane;
    }

    protected GridPane getRoot() {
        return root;
    }

    protected ObjectProperty<Integer> choiceBoxValueProperty() {
        return mazeGridDimChoiceBox.valueProperty();
    }

    protected BooleanProperty animationCheckedProperty() {
        return animationCheckBox.selectedProperty();
    }

    public void disableAlgorithms(boolean predicate) {
        algorithmsVBox.disableProperty().set(predicate);
    }

    public BooleanProperty animationDisableProperty() {
        return animationCheckBox.disableProperty();
    }
}
