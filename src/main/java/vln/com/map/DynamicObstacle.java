package vln.com.map;

import vln.com.graphic.Props;

import java.io.Serializable;
import java.util.List;

public class DynamicObstacle extends Props implements Serializable {
    public int x, y; // Поля для позиции
    private final List<Point> path;
    private int currentPathIndex;

    public DynamicObstacle(int x, int y, List<Point> path) {
        this.x = x;
        this.y = y;
        this.path = path;
        this.currentPathIndex = 0;
        this.design = "\u001B[33m⚠\u001B[0m";
    }

    public Point getNextPosition() {
        int nextIndex = (currentPathIndex + 1) % path.size();
        return path.get(nextIndex);
    }

    public void move() {
        currentPathIndex = (currentPathIndex + 1) % path.size();
        Point next = path.get(currentPathIndex);
        x = next.x;
        y = next.y;
    }

    public List<Point> getPath() {
        return path;
    }

    public record Point(int x, int y) implements Serializable {}
}
