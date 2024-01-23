package com.gen.maze;

import com.gen.maze.data.Tree;
import com.gen.maze.data.UF;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Controller {
    private final Model model;
    private final View view;
    private Rectangle blob;
    private int cellDim;

    public Controller() {
        view = new View();
        model = new Model();

        setUpHandlers();

        drawMazeGrid(null, null, view.choiceBoxValueProperty().get());
    }

    private void setUpHandlers() {
        model.mazeGridDimProperty().bind(view.choiceBoxValueProperty());

        view.choiceBoxValueProperty().addListener(this::drawMazeGrid);
        view.setOnButtonClicked(this::runAlgorithm);
    }

    private void drawMazeGrid(ObservableValue<? extends Number> o, Number _old, Number _new) {
        view.animationDisableProperty().bind(view.choiceBoxValueProperty().isNotEqualTo(20));
        view.animationCheckedProperty().set(false);
        view.disableAlgorithms(false);
        blob = null;

        view.getMazeUIPane().getChildren().clear();

        cellDim = f(_new.intValue());

        model.buildTree();
        model.dfs(this::drawGrid);
    }

    private void drawGrid(Tree.Cell currentCell) {
        var rect = new Rectangle(currentCell.X() * cellDim, currentCell.Y() * cellDim, cellDim, cellDim);
        rect.getStyleClass().add("rectangle");

        view.getMazeUIPane().getChildren().add(rect);
    }

    private void runAlgorithm(MouseEvent mouseEvent) {
        Button dynamicButton = (Button) mouseEvent.getSource();

        switch (dynamicButton.getId()) {
            case "binaryTree" -> Thread.ofPlatform().start(new Task<Void>() {
                @Override
                protected Void call() {
                    view.disableAlgorithms(true);
                    binaryTree();
                    return null;
                }
            });

            case "backtracking" -> Thread.ofPlatform().start(new Task<Void>() {
                @Override
                protected Void call() {
                    view.disableAlgorithms(true);
                    backtracking(new boolean[model.mazeGridDimProperty().get()][model.mazeGridDimProperty().get()], model.getTree().root());
                    return null;
                }
            });

            case "kruskal" -> Thread.ofPlatform().start(new Task<Void>() {
                @Override
                protected Void call() {
                    view.disableAlgorithms(true);
                    kruskal();
                    return null;
                }
            });
        }
    }

    private void kruskal() {
        var uf = new UF<>();
        var walls = new ArrayList<Line>();

        model.dfs(c -> {
            uf.makeSet(c);

            var x = c.X() * cellDim;
            var y = c.Y() * cellDim;
            if (c.hasUp()) walls.add(createLine(x, y, x + cellDim, y));
            if (c.hasDown(model.mazeGridDimProperty().get()))
                walls.add(createLine(x, y + cellDim, x + cellDim, y + cellDim));
            if (c.hasRight(model.mazeGridDimProperty().get()))
                walls.add(createLine(x + cellDim, y, x + cellDim, y + cellDim));
            if (c.hasLeft()) walls.add(createLine(x, y, x, y + cellDim));
        });

        Collections.shuffle(walls);
        walls.forEach(w -> {
            var sets = uf.find(w, Tree.Cell::new, cellDim);
            if (!sets[0].equals(sets[1])) {
                animate(sets[1]);
                Platform.runLater(() -> view.getMazeUIPane().getChildren().add(w));
                uf.union(sets[0], sets[1]);
            }
        });
    }

    private void binaryTree() {
        var random = new SecureRandom();

        model.dfs(c -> {
            animate(c);

            boolean hasDown = c.hasDown(model.mazeGridDimProperty().get());
            boolean hasRight = c.hasRight(model.mazeGridDimProperty().get());

            var x = c.X() * cellDim;
            var y = c.Y() * cellDim;

            if (hasDown && hasRight) {
                switch (random.nextInt(2)) {
                    case 0 ->
                            Platform.runLater(() -> view.getMazeUIPane().getChildren().add(createLine(x, y + cellDim, x + cellDim, y + cellDim)));
                    case 1 ->
                            Platform.runLater(() -> view.getMazeUIPane().getChildren().add(createLine(x + cellDim, y, x + cellDim, y + cellDim)));
                }
            } else if (hasDown)
                Platform.runLater(() -> view.getMazeUIPane().getChildren().add(createLine(x, y + cellDim, x + cellDim, y + cellDim)));
            else if (hasRight)
                Platform.runLater(() -> view.getMazeUIPane().getChildren().add(createLine(x + cellDim, y, x + cellDim, y + cellDim)));
        });
    }

    private void backtracking(boolean[][] visited, Tree.Cell initial) {
        var random = new SecureRandom();
        var stack = new ArrayDeque<Tree.Cell>();

        stack.push(initial);
        visited[initial.Y()][initial.X()] = true;

        while (!stack.isEmpty()) {
            var currentCell = stack.pop();

            animate(currentCell);

            var UN_ADJ = currentCell.getAdjacentCells().stream().filter(c -> !visited[c.Y()][c.X()]).toList();

            if (!UN_ADJ.isEmpty()) {
                stack.push(currentCell);

                var chosenCell = UN_ADJ.get(random.nextInt(UN_ADJ.size()));
                removeWall(currentCell, chosenCell);
                visited[chosenCell.Y()][chosenCell.X()] = true;

                stack.push(chosenCell);
            }
        }
    }

    private void animate(Tree.Cell currentCell) {
        if (view.animationCheckedProperty().get() && view.choiceBoxValueProperty().get() == 20) {
            if (blob == null) {
                blob = new Rectangle();
                Platform.runLater(() -> view.getMazeUIPane().getChildren().add(blob));
            }
            blob.setLayoutY(currentCell.Y() * cellDim + 5);
            blob.setLayoutX(currentCell.X() * cellDim + 5);
            blob.setHeight(cellDim - 10);
            blob.setWidth(cellDim - 10);
            blob.setStroke(Color.BLUE);
            blob.setFill(Color.RED);

            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void removeWall(Tree.Cell currentCell, Tree.Cell chosenCell) {
        var x = currentCell.X() * cellDim;
        var y = currentCell.Y() * cellDim;

        var i = new AtomicInteger(0);

        List.of(currentCell.Y() - 1 == chosenCell.Y(), currentCell.Y() + 1 == chosenCell.Y(), // UP, DOWN
                currentCell.X() + 1 == chosenCell.X(), currentCell.X() - 1 == chosenCell.X() // RIGHT, LEFT
        ).forEach(w -> {
            if (w) switch (i.get()) {
                case 0 -> // UP
                        Platform.runLater(() -> view.getMazeUIPane().getChildren().add(createLine(x, y, x + cellDim, y)));
                case 1 -> // DOWN
                        Platform.runLater(() -> view.getMazeUIPane().getChildren().add(createLine(x, y + cellDim, x + cellDim, y + cellDim)));

                case 2 -> // RIGHT
                        Platform.runLater(() -> view.getMazeUIPane().getChildren().add(createLine(x + cellDim, y, x + cellDim, y + cellDim)));

                case 3 -> // LEFT
                        Platform.runLater(() -> view.getMazeUIPane().getChildren().add(createLine(x, y, x, y + cellDim)));

            }
            i.set(i.get() + 1);
        });
    }

    private Line createLine(int startX, int startY, int endX, int endY) {
        var line = new Line(startX, startY, endX, endY);
        line.setStrokeWidth(2.5);
        line.setStroke(Color.WHITE);

        return line;
    }

    private int f(int dim) {
        return switch (dim) {
            case 20 -> 40;
            case 40 -> 20;

            default -> -1;
        };
    }

    public Region getView() {
        return view.getRoot();
    }
}
