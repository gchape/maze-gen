package com.gen.maze.app;

import com.gen.maze.data.Tree;
import com.gen.maze.data.UF;
import com.gen.maze.data.Wall;
import com.gen.maze.data.misc.Where;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.security.SecureRandom;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Model {
    private final BooleanProperty algorithmInProcess = new SimpleBooleanProperty(false);
    private final IntegerProperty cellDimension = new SimpleIntegerProperty();
    private final IntegerProperty rank = new SimpleIntegerProperty();
    private final Tree tree;

    public Model() {
        tree = new Tree();
    }

    private void insertNodeAt(Map<String, Tree.Cell> states, Tree.Cell c, int y, int x) {
        var state = states.get(y + "(,)" + x);
        if (state == null) {
            var cell = new Tree.Cell(y, x);

            states.put(y + "(,)" + x, cell);
            c.getAdjacentCells().add(cell);
        } else {
            c.getAdjacentCells().add(state);
        }
    }

    public void depthFirstSearch(Consumer<Tree.Cell> applyOnNode, boolean rebuild) {
        if (rebuild) buildTree();

        var stack = new ArrayDeque<Tree.Cell>();
        var visited = new boolean[rank.get()][rank.get()];

        stack.push(tree.root());

        while (!stack.isEmpty()) {
            var c = stack.pop();

            if (!visited[c.Y()][c.X()]) {

                applyOnNode.accept(c);

                visited[c.Y()][c.X()] = true;
                c.getAdjacentCells().stream().filter(c_ -> !visited[c_.Y()][c_.X()]).forEach(stack::push);
            }
        }
    }

    private void aldous_broder(Consumer<Tree.Cell> apply, BiConsumer<Tree.Cell, Where> applyBi) {
        boolean[][] visited = new boolean[rank.get()][rank.get()];

        int tC = visited.length * visited[0].length; // totalCells
        var r = new SecureRandom();
        var curC = tree.root(); // current Cell
        int vC = 0; // visitedCells

        visited[curC.Y()][curC.X()] = true;
        while (vC != tC) {
            var ADJ = curC.getAdjacentCells();
            var newC = ADJ.get(r.nextInt(ADJ.size()));

            if (!visited[newC.Y()][newC.X()]) {
                vC++;

                apply.accept(curC);
                applyBi.accept(curC, getDirection(curC, newC));

                visited[newC.Y()][newC.X()] = true;
            }
            curC = newC;
        }
    }

    private void backtracking(Consumer<Tree.Cell> apply, BiConsumer<Tree.Cell, Where> applyBi) {
        var r = new SecureRandom();
        var s = new ArrayDeque<Tree.Cell>();
        var visited = new boolean[rank.get()][rank.get()];

        s.push(tree.root());
        visited[tree.root().Y()][tree.root().X()] = true;

        while (!s.isEmpty()) {
            var curC = s.pop();
            var UN_ADJ = curC.getAdjacentCells().stream().filter(c_ -> !visited[c_.Y()][c_.X()]).toList();

            apply.accept(curC);

            if (!UN_ADJ.isEmpty()) {
                s.push(curC);

                var newC = UN_ADJ.get(r.nextInt(UN_ADJ.size()));

                applyBi.accept(curC, getDirection(curC, newC));

                visited[newC.Y()][newC.X()] = true;

                s.push(newC);
            }
        }
    }

    private static Where getDirection(Tree.Cell c1, Tree.Cell c2) {
        if (c2.isUpOf(c1)) return Where.UP;
        else if (c2.isDownOf(c1)) return Where.DOWN;
        else if (c2.isRightOf(c1)) return Where.RIGHT;
        else return Where.LEFT;
    }

    private void kruskal(Consumer<Wall> apply) {
        var uf = new UF<>();
        var walls = new ArrayList<Wall>();

        depthFirstSearch(c1 -> {
            uf.makeSet(c1);

            var x = c1.X() * cellDimension.get();
            var y = c1.Y() * cellDimension.get();
            if (c1.hasUp()) walls.add(new Wall(x, y, x + cellDimension.get(), y));
            if (c1.hasDown(rank.get()))
                walls.add(new Wall(x, y + cellDimension.get(), x + cellDimension.get(), y + cellDimension.get()));
            if (c1.hasRight(rank.get()))
                walls.add(new Wall(x + cellDimension.get(), y, x + cellDimension.get(), y + cellDimension.get()));
            if (c1.hasLeft()) walls.add(new Wall(x, y, x, y + cellDimension.get()));
        }, false);

        Collections.shuffle(walls);
        walls.forEach(w -> {
            var sets = uf.find(w, Tree.Cell::new, cellDimension.get());
            if (!sets[0].equals(sets[1])) {

                apply.accept(w);

                uf.union(sets[0], sets[1]);
            }
        });
    }

    private void binaryTree(Consumer<Tree.Cell> apply, BiConsumer<Tree.Cell, Where> applyBi) {
        var r = new SecureRandom();

        depthFirstSearch(c1 -> {
            apply.accept(c1);

            boolean down = c1.hasDown(rank.get());
            boolean right = c1.hasRight(rank.get());

            if (down && right) {
                switch (r.nextInt(2)) {
                    case 0 -> applyBi.accept(c1, Where.DOWN);
                    case 1 -> applyBi.accept(c1, Where.RIGHT);
                }
            } else if (down) applyBi.accept(c1, Where.DOWN);
            else if (right) applyBi.accept(c1, Where.RIGHT);
        }, false);
    }

    private void buildTree() {
        if (!tree.root().getAdjacentCells().isEmpty()) tree.root().getAdjacentCells().clear();

        var nodeTable = new HashMap<String, Tree.Cell>();
        var visited = new boolean[rank.get()][rank.get()];
        var stack = new ArrayDeque<Tree.Cell>();

        nodeTable.put(tree.root().Y() + "(,)" + tree.root().X(), tree.root());
        stack.push(tree.root());

        while (!stack.isEmpty()) {
            var c = stack.pop();

            if (!visited[c.Y()][c.X()]) {
                visited[c.Y()][c.X()] = true;

                List<Boolean> walks = List.of(c.hasUp(), c.hasDown(rank.get()), c.hasRight(rank.get()), c.hasLeft());
                var i = 0;
                for (var w : walks) {
                    if (w) switch (i) {
                        case 0 -> insertNodeAt(nodeTable, c, c.Y() - 1, c.X());
                        case 1 -> insertNodeAt(nodeTable, c, c.Y() + 1, c.X());
                        case 2 -> insertNodeAt(nodeTable, c, c.Y(), c.X() + 1);
                        case 3 -> insertNodeAt(nodeTable, c, c.Y(), c.X() - 1);
                    }
                    i++;
                }
                c.getAdjacentCells().stream().filter(c_ -> !visited[c_.Y()][c_.X()]).forEach(stack::push);
            }
        }
    }

    public void runAlgorithm(String id, Consumer<Tree.Cell> apply1, Consumer<Wall> apply2, BiConsumer<Tree.Cell, Where> applyBi) {
        switch (id) {
            case "0" -> {
                algorithmInProcess.set(true);
                aldous_broder(apply1, applyBi);
            }
            case "1" -> {
                algorithmInProcess.set(true);
                backtracking(apply1, applyBi);
            }
            case "2" -> {
                algorithmInProcess.set(true);
                binaryTree(apply1, applyBi);
            }
            case "3" -> {
                algorithmInProcess.set(true);
                kruskal(apply2);
            }

            case "" -> {
            }
        }
    }

    public IntegerProperty rankProperty() {
        return rank;
    }

    public IntegerProperty cellDimensionProperty() {
        return cellDimension;
    }

    public BooleanProperty algorithmInProcessProperty() {
        return algorithmInProcess;
    }
}

