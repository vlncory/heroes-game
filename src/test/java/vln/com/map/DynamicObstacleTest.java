package vln.com.map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DynamicObstacleTest {
    private DynamicObstacle obstacle;

    @BeforeEach
    void setUp() {
        List<DynamicObstacle.Point> path = List.of(
                new DynamicObstacle.Point(5, 5),
                new DynamicObstacle.Point(5, 6),
                new DynamicObstacle.Point(6, 6)
        );
        obstacle = new DynamicObstacle(5, 5, path);
    }

    @Test
    void testDynamicObstacleCreation() {
        DynamicObstacle obstacle = new DynamicObstacle(1, 1,
                List.of(new DynamicObstacle.Point(1, 1),
                        new DynamicObstacle.Point(2, 2)));

        assertEquals(1, obstacle.x);
        assertEquals(1, obstacle.y);
        assertEquals(2, obstacle.getPath().size());
    }

    @Test
    void testDynamicObstacleInitialization() {
        assertEquals(5, obstacle.x, "Initial x should be 5");
        assertEquals(5, obstacle.y, "Initial y should be 5");
        assertEquals(3, obstacle.getPath().size(), "Path should have 3 points");
        assertEquals("\u001B[33m⚠\u001B[0m", obstacle.design, "Design should be warning symbol");
    }

    @Test
    void testDynamicObstacleMovement() {
        obstacle.move();
        assertEquals(5, obstacle.x, "x should be 5 after first move");
        assertEquals(6, obstacle.y, "y should be 6 after first move");

        obstacle.move();
        assertEquals(6, obstacle.x, "x should be 6 after second move");
        assertEquals(6, obstacle.y, "y should be 6 after second move");

        obstacle.move();
        assertEquals(5, obstacle.x, "x should loop back to 5");
        assertEquals(5, obstacle.y, "y should loop back to 5");
    }

    @Test
    void testGetNextPosition() {
        DynamicObstacle.Point next = obstacle.getNextPosition();
        assertEquals(5, next.x(), "Next x should be 5");
        assertEquals(6, next.y(), "Next y should be 6");

        obstacle.move();
        next = obstacle.getNextPosition();
        assertEquals(6, next.x(), "Next x should be 6");
        assertEquals(6, next.y(), "Next y should be 6");

        obstacle.move();
        next = obstacle.getNextPosition();
        assertEquals(5, next.x(), "Next x should loop back to 5");
        assertEquals(5, next.y(), "Next y should loop back to 5");
    }
}