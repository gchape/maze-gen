package com.gen.maze.app;

import com.gen.maze.data.Tree;
import com.gen.maze.data.Wall;
import com.gen.maze.data.misc.Where;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class View {
    private final ChoiceBox<Integer> choiceBoxGridSize = new ChoiceBox<>(FXCollections.observableList(List.of(20, 40)));
    private final Button btnAldousBroderAlgorithm = new Button("\uD83C\uDF00 Aldous-Broder");
    private final Button btnBinaryTreeAlgorithm = new Button("\uD83C\uDF33 BinaryTree");
    private final Button btnKruskalAlgorithm = new Button("\uD83C\uDF3F Kruskal's");
    private final Button btnBacktrackingAlgorithm = new Button("➰ Backtracking");
    private final CheckBox checkBoxAnimation = new CheckBox("On/Off");
    private final Button btnResetMazeDisplay = new Button("Reset");

    private final AnchorPane paneMazeDisplay = new AnchorPane();
    private final VBox vBoxUserControls = new VBox(15);
    private final VBox vBoxAlgorithms = new VBox(8);
    private final GridPane root = new GridPane();

    private final StringProperty clickedButtonId = new SimpleStringProperty();
    private final IntegerProperty cellDimension = new SimpleIntegerProperty();
    private final BooleanProperty algorithmInProcess = new SimpleBooleanProperty();
    private final BooleanProperty algorithmCancel = new SimpleBooleanProperty(false);

    private Rectangle blob;
    private Canvas canvas;

    public View(BiConsumer<Consumer<Tree.Cell>, Boolean> drawingAlgorithm, IntegerProperty gridSizeProperty, IntegerProperty cellDimensionProperty) {
        glueBindings(gridSizeProperty, cellDimensionProperty);

        setOnChoiceBoxGridSizeChanged(drawingAlgorithm);
        setOnResetButtonClicked();
        setOnAlgorithmsButtonClicked(List.of(btnAldousBroderAlgorithm, btnBacktrackingAlgorithm, btnBinaryTreeAlgorithm, btnKruskalAlgorithm));

        addAlgorithmVBoxChildren();
        addVBoxUserControlsChildren();

        root.addColumn(1, vBoxUserControls);
        root.addColumn(0, paneMazeDisplay);
        choiceBoxGridSize.setValue(20);
    }

    private void glueBindings(IntegerProperty gridSizeProperty, IntegerProperty cellDimensionProperty) {
        gridSizeProperty.bind(choiceBoxGridSize.valueProperty());
        cellDimensionProperty.bind(cellDimension);

        btnResetMazeDisplay.disableProperty().bind(algorithmInProcess.not());

        btnAldousBroderAlgorithm.disableProperty().bind(algorithmInProcess);
        btnBacktrackingAlgorithm.disableProperty().bind(algorithmInProcess);
        btnBinaryTreeAlgorithm.disableProperty().bind(algorithmInProcess);
        btnKruskalAlgorithm.disableProperty().bind(algorithmInProcess);
        choiceBoxGridSize.disableProperty().bind(algorithmInProcess);
        checkBoxAnimation.disableProperty().bind(algorithmInProcess.or(choiceBoxGridSize.valueProperty().isNotEqualTo(20)));
    }

    private void setOnAlgorithmsButtonClicked(List<Button> algorithmBtnList) {
        for (int i = 0; i < algorithmBtnList.size(); i++) {
            algorithmBtnList.get(i).setId(i + "");
        }

        algorithmBtnList.forEach(b -> b.setOnMouseClicked((e) -> {
            var button = (Button) e.getSource();
            clickedButtonId.set(button.getId());
        }));
    }

    private void addVBoxUserControlsChildren() {
        vBoxUserControls.getChildren().addAll(new VBox(3, new Label("[Choose grid size \uD83D\uDCAA]"), choiceBoxGridSize), new VBox(3, new Label("[Animate algs \uD83D\uDD27]"), checkBoxAnimation), new VBox(3, new Label("[Reset maze \uD83D\uDD19]"), btnResetMazeDisplay), vBoxAlgorithms);
    }

    private void addAlgorithmVBoxChildren() {
        vBoxAlgorithms.getChildren().addAll(new Label("[Choose maze algs \uD83D\uDE80]"), btnAldousBroderAlgorithm, btnBacktrackingAlgorithm, btnBinaryTreeAlgorithm, btnKruskalAlgorithm);
    }

    private void setOnResetButtonClicked() {
        btnResetMazeDisplay.setOnMouseClicked((e) -> {
            paneMazeDisplay.getChildren().removeAll(blob);
            algorithmCancel.set(algorithmCancel.not().get());
            algorithmInProcess.set(false);
            clickedButtonId.set("");
            attachNewCanvas();
            blob = null;
        });
    }

    private void attachNewCanvas() {
        paneMazeDisplay.getChildren().remove(canvas);

        canvas = new Canvas(800, 800);
        canvas.getGraphicsContext2D().setStroke(Color.WHITESMOKE);
        canvas.getGraphicsContext2D().setLineWidth(3);

        paneMazeDisplay.getChildren().add(canvas);
    }

    private void setOnChoiceBoxGridSizeChanged(BiConsumer<Consumer<Tree.Cell>, Boolean> drawingAlgorithm) {
        choiceBoxGridSize.valueProperty().addListener((o, v1, v2) -> {
            cellDimension.set(f(v2));
            paneMazeDisplay.getChildren().clear();
            drawingAlgorithm.accept(this::drawRectangle, true);
            attachNewCanvas();
        });
    }

    private void drawRectangle(Tree.Cell cell) {
        var width = cellDimension.get();
        var height = width;

        var rect = new Rectangle(width * cell.X(), height * cell.Y(), width, width);
        rect.getStyleClass().add("rectangle");

        paneMazeDisplay.getChildren().add(rect);
    }

    private int f(int dim) {
        return switch (dim) {
            case 20 -> 40;
            case 40 -> 20;

            default -> -1;
        };
    }

    public void animateCell(Tree.Cell c) {
        if (!checkBoxAnimation.isSelected() || choiceBoxGridSize.getValue() > 20) return;

        var width = cellDimension.get();
        var height = width;

        if (blob == null) {
            blob = new Rectangle(width - 10, height - 10);
            blob.setStyle("-fx-stroke: #00ff38; -fx-fill: #863ae8");
            Platform.runLater(() -> paneMazeDisplay.getChildren().add(blob));
        }
        blob.setLayoutY(c.Y() * height + 5);
        blob.setLayoutX(c.X() * width + 5);

        try {
            Thread.sleep(25);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void animateWall(Wall w) {
        graphics().strokeLine(w.startX(), w.startY(), w.endX(), w.endY());

        if (checkBoxAnimation.isSelected() && choiceBoxGridSize.getValue() == 20) try {
            Thread.sleep(25);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void eraseWall(Tree.Cell c, Where where) {
        var width = cellDimension.get();
        var graphics = graphics();
        var height = width;

        var x = c.X() * width;
        var y = c.Y() * height;

        switch (where) {
            case Where.UP -> graphics.strokeLine(x, y, x + width, y);
            case Where.DOWN -> graphics.strokeLine(x, y + height, x + width, y + height);
            case Where.RIGHT -> graphics.strokeLine(x + width, y, x + width, y + height);
            case Where.LEFT -> graphics.strokeLine(x, y, x, y + height);
        }
    }


    public BooleanProperty algorithmCancelProperty() {
        return algorithmCancel;
    }

    public StringProperty clickedButtonIdProperty() {
        return clickedButtonId;
    }

    public BooleanProperty algorithmInProcessProperty() {
        return algorithmInProcess;
    }

    private GraphicsContext graphics() {
        return canvas.getGraphicsContext2D();
    }

    public Region getRoot() {
        return root;
    }
}
