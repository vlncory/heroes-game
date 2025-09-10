package vln.com.map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DynamicObstaclesConfigTest {

    @TempDir
    Path tempDir;

    private String mapName;
    private List<DynamicObstaclesConfig.ObstacleData> testObstacles;

    @BeforeEach
    void setUp() {
        mapName = "testMap";
        testObstacles = new ArrayList<>();
        List<DynamicObstacle.Point> path = new ArrayList<>();
        path.add(new DynamicObstacle.Point(1, 1));
        path.add(new DynamicObstacle.Point(2, 2));
        testObstacles.add(new DynamicObstaclesConfig.ObstacleData(0, 0, path));
    }

    @Test
    void testSaveToXML() throws Exception {
        // Arrange
        File mapsDir = new File(tempDir.toFile(), "src/main/resources/maps");
        assertTrue(mapsDir.mkdirs(), "Maps directory should be created");
        System.setProperty("user.dir", tempDir.toString());

        // Act
        DynamicObstaclesConfig.saveToXML(mapName, testObstacles);

        // Assert
        File outputFile = new File(mapsDir, mapName + "_dynamic.xml");
        assertTrue(outputFile.exists(), "XML file should be created");

        String content = Files.readString(outputFile.toPath());
        assertTrue(content.contains("<dynamicObstacles>"), "XML should contain root element");
        assertTrue(content.contains("<start x=\"0\" y=\"0\"/>"), "XML should contain start position");
        assertTrue(content.contains("<point x=\"1\" y=\"1\"/>"), "XML should contain path point 1");
        assertTrue(content.contains("<point x=\"2\" y=\"2\"/>"), "XML should contain path point 2");
    }

    @Test
    void testLoadFromXML() throws Exception {
        // Arrange
        File mapsDir = new File(tempDir.toFile(), "src/main/resources/maps");
        assertTrue(mapsDir.mkdirs(), "Maps directory should be created");
        System.setProperty("user.dir", tempDir.toString());
        DynamicObstaclesConfig.saveToXML(mapName, testObstacles);

        // Act
        List<DynamicObstaclesConfig.ObstacleData> loadedObstacles = DynamicObstaclesConfig.loadFromXML(mapName);

        // Assert
        assertEquals(1, loadedObstacles.size(), "Should load one obstacle");
        DynamicObstaclesConfig.ObstacleData loaded = loadedObstacles.getFirst();
        assertEquals(0, loaded.startX(), "Start X should match");
        assertEquals(0, loaded.startY(), "Start Y should match");
        assertEquals(2, loaded.path().size(), "Path should have 2 points");
        assertEquals(1, loaded.path().get(0).x(), "First point X should match");
        assertEquals(1, loaded.path().get(0).y(), "First point Y should match");
        assertEquals(2, loaded.path().get(1).x(), "Second point X should match");
        assertEquals(2, loaded.path().get(1).y(), "Second point Y should match");
    }

    @Test
    void testLoadFromNonExistentXML() throws Exception {
        // Arrange
        System.setProperty("user.dir", tempDir.toString());

        // Act
        List<DynamicObstaclesConfig.ObstacleData> loadedObstacles = DynamicObstaclesConfig.loadFromXML("nonexistent");

        // Assert
        assertTrue(loadedObstacles.isEmpty(), "Should return empty list for non-existent file");
    }
}