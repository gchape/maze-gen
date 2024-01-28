package com.gen.maze.data;

import java.util.Objects;

public record Wall(double startX, double startY, double endX, double endY) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wall wall = (Wall) o;
        return Double.compare(startX, wall.startX) == 0 && Double.compare(startY, wall.startY) == 0 && Double.compare(endX, wall.endX) == 0 && Double.compare(endY, wall.endY) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startX, startY, endX, endY);
    }
}
