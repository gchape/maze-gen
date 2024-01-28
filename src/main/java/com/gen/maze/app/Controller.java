package com.gen.maze.app;

import com.gen.maze.data.Tree;
import com.gen.maze.data.UF;
import com.gen.maze.data.Wall;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Controller {
    private final BooleanProperty mazeGridResetBtnDisableProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty doesAlgorithmExecutedProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty animationDisableProperty = new SimpleBooleanProperty();
    private Thread algorithmThread;
    private final Model model;
    private final View view;
    private Rectangle blob;
    private int cellDim;

    public Controller() {
        view = new View();
        model = new Model();

        prepareBindings();
        setUpHandlers();

        drawMazeGrid(null, null, view.choiceBoxValueProperty().get());
    }

    public Region getView() {
        return view.getRoot();
    }

    private void runAlgorithm(MouseEvent mouseEvent) {
        Button clickedBtn = (Button) mouseEvent.getSource();

        switch (clickedBtn.getId()) {
            case "binaryTree" -> algorithmThread = Thread.ofPlatform().start(new Task<Void>() {
                @Override
                protected Void call() {
                    doesAlgorithmExecutedProperty.set(true);
                    binaryTree();
                    return null;
                }
            });

            case "backtracking" -> algorithmThread = Thread.ofPlatform().start(new Task<Void>() {
                @Override
                protected Void call() {
                    doesAlgorithmExecutedProperty.set(true);
                    backtracking(new boolean[model.mazeGridDimProperty().get()][model.mazeGridDimProperty().get()], model.getTree().root());
                    return null;
                }
            });

            case "kruskal" -> algorithmThread = Thread.ofPlatform().start(new Task<Void>() {
                @Override
                protected Void call() {
                    doesAlgorithmExecutedProperty.set(true);
                    kruskal();
                    return null;
                }
            });

            case "aldous-broder" -> algorithmThread = Thread.ofPlatform().start(new Task<Void>() {
                @Override
                protected Void call() {
                    doesAlgorithmExecutedProperty.set(true);
                    aldous_broder(new boolean[model.mazeGridDimProperty().get()][model.mazeGridDimProperty().get()], model.getTree().root());
                    return null;
                }
            });

            //TODO Prim
        }
    }

    private void resetBtnClickEventTask(MouseEvent ignore) {
        if (algorithmThread != null) algorithmThread.interrupt();
        view.animationCheckedProperty().set(false);
        doesAlgorithmExecutedProperty.set(false);
        view.getMazeUIPane().getChildren().remove(blob);
        view.getMazeUIPane().getChildren().add(view.getCanvas());
        blob = null;
    }

    private void drawMazeGrid(ObservableValue<? extends Number> o, Number oldV, Number newV) {
        view.animationCheckedProperty().set(false);
        doesAlgorithmExecutedProperty.set(false);

        cellDim = f(newV.intValue());

        view.getMazeUIPane().getChildren().clear();
        model.buildTree();
        model.dfs(this::drawRectangle);
        view.getMazeUIPane().getChildren().add(view.getCanvas());
    }

    private void drawRectangle(Tree.Cell currentCell) {
        var rect = new Rectangle(currentCell.X() * cellDim, currentCell.Y() * cellDim, cellDim, cellDim);
        rect.getStyleClass().add("rectangle");

        view.getMazeUIPane().getChildren().add(rect);
    }

    private void aldous_broder(boolean[][] visited, Tree.Cell curC) {
        int tC = visited.length * visited[0].length; // totalCells
        var r = new SecureRandom();
        int vC = 0; // visitedCells

        visited[curC.Y()][curC.X()] = true;
        while (vC != tC) {
            var ADJ = curC.getAdjacentCells();
            var newC = ADJ.get(r.nextInt(ADJ.size()));

            if (!visited[newC.Y()][newC.X()]) {
                vC++;
                animate(curC);
                removeWall(curC, newC);
                visited[newC.Y()][newC.X()] = true;
            }
            curC = newC;
        }
    }

    private void backtracking(boolean[][] visited, Tree.Cell c) {
        var r = new SecureRandom();
        var s = new ArrayDeque<Tree.Cell>();

        s.push(c);
        visited[c.Y()][c.X()] = true;

        while (!s.isEmpty()) {
            var curC = s.pop();
            var UN_ADJ = curC.getAdjacentCells().stream().filter(c_ -> !visited[c_.Y()][c_.X()]).toList();

            animate(curC);

            if (!UN_ADJ.isEmpty()) {
                s.push(curC);

                var newC = UN_ADJ.get(r.nextInt(UN_ADJ.size()));
                removeWall(curC, newC);
                visited[newC.Y()][newC.X()] = true;

                s.push(newC);
            }
        }
    }

    private void binaryTree() {
        var r = new SecureRandom();

        model.dfs(c -> {
            animate(c);

            boolean down = c.hasDown(model.mazeGridDimProperty().get());
            boolean right = c.hasRight(model.mazeGridDimProperty().get());

            var x = c.X() * cellDim;
            var y = c.Y() * cellDim;

            if (down && right) {
                switch (r.nextInt(2)) {
                    case 0 -> view.graphics().strokeLine(x, y + cellDim, x + cellDim, y + cellDim);
                    case 1 -> view.graphics().strokeLine(x + cellDim, y, x + cellDim, y + cellDim);
                }
            } else if (down) view.graphics().strokeLine(x, y + cellDim, x + cellDim, y + cellDim);
            else if (right) view.graphics().strokeLine(x + cellDim, y, x + cellDim, y + cellDim);
        });
    }

    private void kruskal() {
        var uf = new UF<>();
        var walls = new ArrayList<Wall>();

        model.dfs(c -> {
            uf.makeSet(c);

            var x = c.X() * cellDim;
            var y = c.Y() * cellDim;
            if (c.hasUp()) walls.add(new Wall(x, y, x + cellDim, y));
            if (c.hasDown(model.mazeGridDimProperty().get()))
                walls.add(new Wall(x, y + cellDim, x + cellDim, y + cellDim));
            if (c.hasRight(model.mazeGridDimProperty().get()))
                walls.add(new Wall(x + cellDim, y, x + cellDim, y + cellDim));
            if (c.hasLeft()) walls.add(new Wall(x, y, x, y + cellDim));
        });

        Collections.shuffle(walls);
        walls.forEach(w -> {
            var sets = uf.find(w, Tree.Cell::new, cellDim);
            if (!sets[0].equals(sets[1])) {
                animate(w);
                uf.union(sets[0], sets[1]);
            }
        });
    }

    private void removeWall(Tree.Cell c1, Tree.Cell c2) {
        var x = c1.X() * cellDim;
        var y = c1.Y() * cellDim;

        var i = 0;
        for (var w : List.of(c1.Y() - 1 == c2.Y(), c1.Y() + 1 == c2.Y(), c1.X() + 1 == c2.X(), c1.X() - 1 == c2.X())) {
            if (w) switch (i) {
                case 0 -> view.graphics().strokeLine(x, y, x + cellDim, y);
                case 1 -> view.graphics().strokeLine(x, y + cellDim, x + cellDim, y + cellDim);
                case 2 -> view.graphics().strokeLine(x + cellDim, y, x + cellDim, y + cellDim);
                case 3 -> view.graphics().strokeLine(x, y, x, y + cellDim);
            }
            i++;
        }
    }

    private void animate(Tree.Cell c, boolean guard) {
        if (guard) {
            blob = new Rectangle(cellDim - 10, cellDim - 10);
            blob.setStyle("-fx-stroke: #00ff38; -fx-fill: azure");
            Platform.runLater(() -> view.getMazeUIPane().getChildren().add(blob));
        }
        blob.setLayoutY(c.Y() * cellDim + 5);
        blob.setLayoutX(c.X() * cellDim + 5);

        try {
            Thread.sleep(25);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void animate(Tree.Cell c) {
        if (view.animationCheckedProperty().get() && view.choiceBoxValueProperty().get() == 20)
            animate(c, blob == null);
    }

    private void animate(Wall w) {
        if (view.animationCheckedProperty().get() && view.choiceBoxValueProperty().get() == 20) {
            view.graphics().strokeLine(w.startX(), w.startY(), w.endX(), w.endY());

            try {
                Thread.sleep(40);
            } catch (InterruptedException ignore) {
            }
        } else view.graphics().strokeLine(w.startX(), w.startY(), w.endX(), w.endY());
    }

    private int f(int dim) {
        return switch (dim) {
            case 20 -> 40;
            case 40 -> 20;

            default -> -1;
        };
    }

    private void setUpHandlers() {
        view.setOnBtnResetMazeGridClicked(this::resetBtnClickEventTask);
        view.choiceBoxValueProperty().addListener(this::drawMazeGrid);
        view.setOnBtnAlgorithmsClicked(this::runAlgorithm);
    }

    private void prepareBindings() {
        model.mazeGridDimProperty().bind(view.choiceBoxValueProperty());

        animationDisableProperty.bind(view.choiceBoxValueProperty().isNotEqualTo(20).or(doesAlgorithmExecutedProperty));
        view.mazeGridResetBtnDisableProperty().bind(mazeGridResetBtnDisableProperty);
        mazeGridResetBtnDisableProperty.bind(doesAlgorithmExecutedProperty.not());
        view.algorithmsVBoxDisableProperty().bind(doesAlgorithmExecutedProperty);
        view.choiceBoxDisableProperty().bind(doesAlgorithmExecutedProperty);
        view.animationDisableProperty().bind(animationDisableProperty);
    }
}
