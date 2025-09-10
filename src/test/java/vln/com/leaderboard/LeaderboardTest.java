package vln.com.leaderboard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.util.List;
import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LeaderboardTest {

    private Leaderboard leaderboard;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        leaderboard = new Leaderboard();
    }

    // Test 7: Корректность обновления рекордов
    @Test
    void testAddRecordNewUser() {
        leaderboard.addRecord("user1", 100, "testMap");
        List<Leaderboard.Record> records = leaderboard.getTopRecords(5);

        assertEquals(1, records.size(), "Should have one record");
        assertEquals("user1", records.getFirst().username(), "Username should match");
        assertEquals(100, records.getFirst().score(), "Score should match");
        assertEquals("testMap", records.getFirst().mapName(), "Map name should match");
    }

    @Test
    void testAddRecordUpdateExistingUser() {
        leaderboard.addRecord("user1", 100, "testMap");
        leaderboard.addRecord("user1", 200, "newMap");

        List<Leaderboard.Record> records = leaderboard.getTopRecords(5);
        assertEquals(1, records.size(), "Should have one record after update");
        assertEquals("user1", records.getFirst().username(), "Username should match");
        assertEquals(200, records.getFirst().score(), "Score should be updated to 200");
        assertEquals("newMap", records.getFirst().mapName(), "Map name should be updated");
    }

    @Test
    void testAddMultipleRecordsSorting() {
        leaderboard.addRecord("user1", 100, "map1");
        leaderboard.addRecord("user2", 300, "map2");
        leaderboard.addRecord("user3", 200, "map3");

        List<Leaderboard.Record> records = leaderboard.getTopRecords(5);
        assertEquals(3, records.size(), "Should have three records");
        assertEquals("user2", records.get(0).username(), "Highest score should be first");
        assertEquals(300, records.get(0).score(), "First score should be 300");
        assertEquals("user3", records.get(1).username(), "Second highest score should be second");
        assertEquals(200, records.get(1).score(), "Second score should be 200");
        assertEquals("user1", records.get(2).username(), "Lowest score should be last");
        assertEquals(100, records.get(2).score(), "Last score should be 100");
    }

    @Test
    void testSaveAndLoadLeaderboard() throws Exception {
        leaderboard.addRecord("user1", 100, "map1");
        leaderboard.addRecord("user2", 200, "map2");

        File leaderboardFile = tempDir.resolve("src/main/resources/leaderboard/leaderboard.dat").toFile();
        System.setProperty("user.dir", tempDir.toString());
        leaderboard.saveToFile();

        assertTrue(leaderboardFile.exists(), "Leaderboard file should be created");

        Leaderboard loaded = Leaderboard.loadFromFile();
        List<Leaderboard.Record> records = loaded.getTopRecords(5);

        assertEquals(2, records.size(), "Should load two records");
        assertEquals("user2", records.get(0).username(), "First record username should match");
        assertEquals(200, records.get(0).score(), "First record score should match");
        assertEquals("user1", records.get(1).username(), "Second record username should match");
        assertEquals(100, records.get(1).score(), "Second record score should match");
    }
}