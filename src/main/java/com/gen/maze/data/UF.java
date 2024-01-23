package com.gen.maze.data;

import javafx.scene.shape.Line;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class UF<T extends Tree.Cell> {
    private final Map<T, T> parent;
    private final Map<T, Integer> rank;

    public UF() {
        parent = new HashMap<>();
        rank = new HashMap<>();
    }

    public void makeSet(T element) {
        parent.put(element, element);
        rank.put(element, 0);
    }

    @SuppressWarnings("unchecked")
    public T[] find(Line w, BiFunction<Integer, Integer, T> biFunction, int cellDim) {
        T c1, c2;
        if (w.getStartY() != w.getEndY()) {
            c1 = biFunction.apply((int) w.getStartY() / cellDim, (int) (w.getStartX() / cellDim) - 1);
            c2 = biFunction.apply((int) w.getStartY() / cellDim, (int) w.getStartX() / cellDim);
        } else {
            c1 = biFunction.apply((int) w.getStartY() / cellDim, (int) w.getStartX() / cellDim);
            c2 = biFunction.apply((int) (w.getStartY() / cellDim) - 1, (int) w.getStartX() / cellDim);
        }

        return (T[]) new Tree.Cell[]{find(c1), find(c2)};
    }

    public T find(T element) {
        if (!element.equals(parent.get(element))) {
            parent.put(element, find(parent.get(element))); // Path compression
        }

        return parent.get(element);
    }

    public void union(T set1, T set2) {
        T root1 = find(set1);
        T root2 = find(set2);

        if (!root1.equals(root2)) {
            if (rank.get(root1) < rank.get(root2)) {
                parent.put(root1, root2);
            } else if (rank.get(root1) > rank.get(root2)) {
                parent.put(root2, root1);
            } else {
                parent.put(root2, root1);
                rank.put(root1, rank.get(root1) + 1);
            }
        }
    }
}
