package com.gen.maze.data;

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

    public T find(T element) {
        if (!element.equals(parent.get(element))) {
            parent.put(element, find(parent.get(element))); // Path compression
        }

        return parent.get(element);
    }

    public void makeSet(T element) {
        parent.put(element, element);
        rank.put(element, 0);
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

    @SuppressWarnings("unchecked")
    public T[] find(Wall w, BiFunction<Integer, Integer, T> biFunction, int cellDim) {
        T c1, c2;
        if (w.startY() != w.endY()) {
            c1 = biFunction.apply((int) w.startY() / cellDim, (int) (w.startX() / cellDim) - 1);
            c2 = biFunction.apply((int) w.startY() / cellDim, (int) w.startX() / cellDim);
        } else {
            c1 = biFunction.apply((int) w.startY() / cellDim, (int) w.startX() / cellDim);
            c2 = biFunction.apply((int) (w.startY() / cellDim) - 1, (int) w.startX() / cellDim);
        }

        return (T[]) new Tree.Cell[]{find(c1), find(c2)};
    }
}
