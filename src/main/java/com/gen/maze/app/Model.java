package com.gen.maze.app;

import com.gen.maze.data.Tree;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Model {
    private final Tree tree;
    private final SimpleIntegerProperty mazeGridDimProperty = new SimpleIntegerProperty();

    public Model() {
        tree = new Tree();
    }

    public void buildTree() {
        tree.root().getAdjacentCells().clear();

        var visited = new boolean[mazeGridDimProperty.get()][mazeGridDimProperty.get()];
        var states = new HashMap<String, Tree.Cell>();
        var stack = new ArrayDeque<Tree.Cell>();

        states.put(tree.root().Y() + "(,)" + tree.root().X(), tree.root());
        stack.push(tree.root());

        while (!stack.isEmpty()) {
            var c = stack.pop();

            if (!visited[c.Y()][c.X()]) {
                var i = new AtomicInteger(0);

                List.of(c.hasUp(), c.hasDown(mazeGridDimProperty.get()), c.hasRight(mazeGridDimProperty.get()), c.hasLeft()).forEach(w -> {
                    if (w) switch (i.get()) {
                        case 0 -> act(states, c, c.Y() - 1, c.X());
                        case 1 -> act(states, c, c.Y() + 1, c.X());
                        case 2 -> act(states, c, c.Y(), c.X() + 1);
                        case 3 -> act(states, c, c.Y(), c.X() - 1);
                    }
                    i.incrementAndGet();
                });
            }

            visited[c.Y()][c.X()] = true;
            c.getAdjacentCells().stream().filter(c_ -> !visited[c_.Y()][c_.X()]).forEach(stack::push);
        }
    }

    private void act(Map<String, Tree.Cell> states, Tree.Cell c, int y, int x) {
        var state = states.get(y + "(,)" + x);
        if (state == null) {
            var cell = new Tree.Cell(y, x);

            states.put(y + "(,)" + x, cell);
            c.getAdjacentCells().add(cell);
        } else {
            c.getAdjacentCells().add(state);
        }
    }

    public void dfs(Consumer<Tree.Cell> process) {
        var stack = new ArrayDeque<Tree.Cell>();
        var visited = new boolean[mazeGridDimProperty.get()][mazeGridDimProperty.get()];

        stack.push(tree.root());

        while (!stack.isEmpty()) {
            var c = stack.pop();

            if (!visited[c.Y()][c.X()]) process.accept(c);

            visited[c.Y()][c.X()] = true;

            c.getAdjacentCells().stream().filter(c_ -> !visited[c_.Y()][c_.X()]).forEach(stack::push);
        }
    }

    public SimpleIntegerProperty mazeGridDimProperty() {
        return mazeGridDimProperty;
    }

    public Tree getTree() {
        return tree;
    }
}

