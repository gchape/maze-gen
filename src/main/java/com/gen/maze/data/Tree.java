package com.gen.maze.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Tree {
    public static class Cell {
        private final int y, x;
        private List<Cell> adjacentCells;

        public Cell(int y, int x) {
            this.y = y;
            this.x = x;
            adjacentCells = new ArrayList<>();
        }

        public int Y() {
            return y;
        }

        public int X() {
            return x;
        }

        public List<Cell> getAdjacentCells() {
            return adjacentCells;
        }

        public void newAdjacentCellsList() {
            adjacentCells = new ArrayList<>();
        }

        public boolean hasUp() {
            return y - 1 >= 0;
        }

        public boolean hasDown(int dim) {
            return y + 1 < dim;
        }

        public boolean hasRight(int dim) {
            return x + 1 < dim;
        }

        public boolean hasLeft() {
            return x - 1 >= 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Cell cell = (Cell) o;
            return y == cell.y && x == cell.x;
        }

        @Override
        public int hashCode() {
            return Objects.hash(y, x);
        }
    }

    private final Cell root;

    public Tree() {
        this.root = new Cell(0, 0);
    }

    public Cell root() {
        return root;
    }
}
