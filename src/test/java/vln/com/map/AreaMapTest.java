package vln.com.map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import vln.com.buildings.PlayerCastle;
import vln.com.graphic.Props;
import vln.com.units.Hero;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.*;

import static org.junit.jupiter.api.Assertions.*;

import vln.com.buildings.*;
import vln.com.units.SmokeUnit;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Scanner;

class AreaMapTest {
    private static final Logger logger = Logger.getLogger(AreaMapTest.class.getName());
    private static final String LOG_FILE = "src/test/logs/Map.log";

    static {
        try {
            FileHandler fh = new FileHandler(LOG_FILE, true);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setLevel(Level.ALL);
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Failed to initialize logger for AreaMapTest", e);
        }
    }

    private AreaMap map;
    private Hero[] heroes;
    private Scanner scanner;
    private StringBuilder input;
    private final int height = 10;
    private final int width = 10;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        logger.info("[AreaMapTest] Setting up AreaMap instance");
        heroes = new Hero[]{new Hero(100), new Hero(100)};
        heroes[0].setSymbol("\u001B[34m♡\u001B[0m");
        heroes[0].isPlayer = true;
        heroes[0].isAvailable = true;
        heroes[0].heroX = 1;
        heroes[0].heroY = 5;
        heroes[0].army = new HashMap<>();
        heroes[0].army.put("Lancer", 10);
        heroes[0].army.put("Archer", 10);
        heroes[1].setSymbol("\u001B[31m♡\u001B[0m");
        heroes[1].heroX = 8;
        heroes[1].heroY = 5;
        heroes[1].army = new HashMap<>();
        heroes[1].army.put("Lancer", 6);
        heroes[1].army.put("Archer", 3);
        map = new AreaMap(10, 10, heroes, true, "testUser");
        input = new StringBuilder();
        scanner = new Scanner(new StringReader(""));
    }

    private void setInput(String... inputs) {
        input.setLength(0);
        for (String s : inputs) {
            input.append(s).append("\n");
        }
        scanner = new Scanner(new StringReader(input.toString()));
    }

    // Test 1: Корректность редактирования карты – проверка на координаты и т.д.
    @Test
    void testMapEditingValidCoordinates() {
        logger.info("[AreaMapTest] testMapEditingValidCoordinates start");
        try {
            // Initialize map without heroes to avoid interference
            map = new AreaMap(10, 10, null, false, "testUser");
            setInput(
                    "d", // Move right from (5,5) to (5,6)
                    "t", // Place Trail
                    "s", // Move down to (6,6)
                    "o", // Place Obstacle
                    "a", // Move left to (6,5)
                    "f", // Place Field
                    "q"  // Quit
            );

            // Log initial cursor position
            int cursorY = height / 2;
            int cursorX = width / 2;
            logger.info("Initial cursor position: (" + cursorY + "," + cursorX + ")");

            // Simulate editMode with logging
            map.editMode(scanner);

            Props[][] world = getWorld();
            // Debug actual types
            logger.info("Type at (5,6): " + (world[5][6] != null ? world[5][6].getClass().getSimpleName() : "null"));
            logger.info("Type at (6,6): " + (world[6][6] != null ? world[6][6].getClass().getSimpleName() : "null"));
            logger.info("Type at (6,5): " + (world[6][5] != null ? world[6][5].getClass().getSimpleName() : "null"));

            assertInstanceOf(vln.com.map.Trail.class, world[5][6], "Should place Trail at (5,6)");
            assertInstanceOf(vln.com.map.Obstacle.class, world[6][6], "Should place Obstacle at (6,6)");
            assertInstanceOf(vln.com.map.Field.class, world[6][5], "Should place Field at (6,5)");
            logger.info("[AreaMapTest] testMapEditingValidCoordinates passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testMapEditingValidCoordinates failed", t);
            throw t;
        }
    }

    @Test
    void testMapEditingInvalidCastlePosition() {
        setInput(
                "a", // Move to (5,0) - castle position
                "t", // Try to place Trail
                "q"  // Quit
        );
        map.editMode(scanner);

        Props[][] world = getWorld();
        assertInstanceOf(PlayerCastle.class, world[5][0], "Castle at (5,0) should not be overwritten");
    }

    @Test
    void testMapEditingBoundaryCoordinates() {
        logger.info("[AreaMapTest] testMapEditingBoundaryCoordinates start");
        try {
            map = new AreaMap(10, 10, null, false, "testUser");
            setInput(
                    "w", // Move up from (5,5) to (4,5)
                    "t", // Place Trail
                    "a", // Move left to (4,4)
                    "o", // Place Obstacle
                    "q"  // Quit
            );
            map.editMode(scanner);

            Props[][] world = getWorld();
            logger.info("Type at (4,5): " + (world[4][5] != null ? world[4][5].getClass().getSimpleName() : "null"));
            logger.info("Type at (4,4): " + (world[4][4] != null ? world[4][4].getClass().getSimpleName() : "null"));
            assertInstanceOf(Trail.class, world[4][5], "Should place Trail at (4,5)");
            assertInstanceOf(Obstacle.class, world[4][4], "Should place Obstacle at (4,4)");
            logger.info("[AreaMapTest] testMapEditingBoundaryCoordinates passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testMapEditingBoundaryCoordinates failed", t);
            throw t;
        }
    }

    // Test 2: Корректность сохранения карты
    @Test
    void testMapSaving() throws IOException {
        logger.info("[AreaMapTest] testMapSaving start");
        try {
            File mapsDir = new File(tempDir.toFile(), "src/main/resources/maps");
            assertTrue(mapsDir.mkdirs(), "Maps directory should be created");
            System.setProperty("user.dir", tempDir.toString());

            map = new AreaMap(10, 10, null, false, "testUser");
            setInput(
                    "d", // Move to (5,6)
                    "t", // Place Trail
                    "e", // Save
                    "testMap", // Map name
                    "q"  // Quit
            );
            map.editMode(scanner);

            Path mapFile = tempDir.resolve("src/main/resources/maps/testMap.map");
            map.saveToFile("testMap");

            assertTrue(Files.exists(mapFile), "Map file should be created");
            List<String> lines = Files.readAllLines(mapFile);
            assertEquals("10 10", lines.get(0), "Map size should be 10x10");
            assertEquals('T', lines.get(6).charAt(6), "Trail should be at (5,6) in map file");
            logger.info("[AreaMapTest] testMapSaving passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testMapSaving failed", t);
            throw t;
        }
    }

    // Test 3: Корректность изменения существующей карты
    @Test
    void testMapEditingExistingMap() throws IOException {
        logger.info("[AreaMapTest] testMapEditingExistingMap start");
        try {
            Path mapFile = tempDir.resolve("src/main/resources/maps/testMap.map");
            Files.createDirectories(mapFile.getParent());
            Files.writeString(mapFile,
                    """
                            10 10
                            FFFFFFFFFF
                            FFFFFFFFFF
                            FFFFFFFFFF
                            FFFFFFFFFF
                            FFFFFFFFFF
                            OTFFFFFFFB
                            FFFFFFFFFF
                            FFFFFFFFFF
                            FFFFFFFFFF
                            FFFFFFFFFF
                            """
            );

            System.setProperty("user.dir", tempDir.toString());
            AreaMap loadedMap = new AreaMap("testMap", null, "testUser");

            setInput(
                    "a", // Move to (5,4)
                    "a", // Move to (5,3)
                    "a", // Move to (5,2)
                    "o", // Place Obstacle
                    "e", // Save
                    "q"  // Quit
            );
            loadedMap.editMode(scanner);

            loadedMap = new AreaMap("testMap", null, "testUser");
            Props[][] world = getWorld(loadedMap);

            logger.info("Type at (5,1): " + (world[5][1] != null ? world[5][1].getClass().getSimpleName() : "null"));
            logger.info("Type at (5,2): " + (world[5][2] != null ? world[5][2].getClass().getSimpleName() : "null"));
            logger.info("Type at (5,0): " + (world[5][0] != null ? world[5][0].getClass().getSimpleName() : "null"));
            logger.info("Type at (5,9): " + (world[5][9] != null ? world[5][9].getClass().getSimpleName() : "null"));

            assertInstanceOf(Trail.class, world[5][1], "Trail should remain at (5,1)");
            assertInstanceOf(Obstacle.class, world[5][2], "Obstacle should be placed at (5,2) after editing");
            assertInstanceOf(PlayerCastle.class, world[5][0], "PlayerCastle should be at (5,0)");
            assertInstanceOf(BotCastle.class, world[5][9], "BotCastle should be at (5,9)");
            logger.info("[AreaMapTest] testMapEditingExistingMap passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testMapEditingExistingMap failed", t);
            throw t;
        }
    }

    // Test 4: Корректность загрузки карты
    @Test
    void testMapLoading() throws IOException {
        Path mapFile = tempDir.resolve("src/main/resources/maps/testMap.map");
        Files.createDirectories(mapFile.getParent());
        Files.writeString(mapFile,
                """
                        10 10
                        FFFFFFFFFF
                        FFFFFFFFFF
                        FFFFFFFFFF
                        FFFFFFFFFF
                        FFFFFFFFFF
                        OTFFFFFFFB
                        FFFFFFFFFF
                        FFFFFFFFFF
                        FFFFFFFFFF
                        FFFFFFFFFF
                        """
        );

        System.setProperty("user.dir", tempDir.toString());
        AreaMap loadedMap = new AreaMap("testMap", null, "testUser");

        Props[][] world = getWorld(loadedMap);
        assertInstanceOf(Trail.class, world[5][1], "Trail should be loaded at (5,1)");
        assertInstanceOf(PlayerCastle.class, world[5][0], "PlayerCastle should be loaded at (5,0)");
        assertInstanceOf(BotCastle.class, world[5][9], "BotCastle should be loaded at (5,9)");
    }

    // Test 5: Корректность загрузки сохранения пользователя
    @Test
    void testLoadUserSave() throws IOException, ClassNotFoundException {
        // Modify hero state
        heroes[0].gold = 50;
        heroes[0].army.put("Lancer", 15);
        heroes[1].gold = 30;
        heroes[1].army.put("Archer", 5);

        // Save game
        Path saveDir = tempDir.resolve("src/main/saves/testUser");
        Files.createDirectories(saveDir);
        System.setProperty("user.dir", tempDir.toString());
        map.saveGame("testSave", heroes);

        // Load game
        Hero[] newHeroes = new Hero[]{new Hero(100), new Hero(100)};
        AreaMap loadedMap = AreaMap.loadGame("testSave", "testUser", newHeroes);

        // Verify map
        Props[][] loadedWorld = getWorld(loadedMap);
        assertInstanceOf(PlayerCastle.class, loadedWorld[5][0], "PlayerCastle should be at (5,0)");
        assertInstanceOf(BotCastle.class, loadedWorld[5][9], "BotCastle should be at (5,9)");
        assertInstanceOf(Hero.class, loadedWorld[5][1], "Player hero should be at (5,1)");
        assertInstanceOf(Hero.class, loadedWorld[5][8], "Bot hero should be at (5,8)");

        // Verify hero state
        assertEquals(50, newHeroes[0].gold, "Player hero gold should be restored");
        assertEquals(15, newHeroes[0].army.get("Lancer"), "Player hero Lancer count should be restored");
        assertEquals(30, newHeroes[1].gold, "Bot hero gold should be restored");
        assertEquals(5, newHeroes[1].army.get("Archer"), "Bot hero Archer count should be restored");
    }

    @Test
    void testLoadUserSaveWrongUser() throws IOException {
        heroes[0].gold = 50;
        Path saveDir = tempDir.resolve("src/main/saves/testUser");
        Files.createDirectories(saveDir);
        System.setProperty("user.dir", tempDir.toString());
        map.saveGame("testSave", heroes);

        Hero[] newHeroes = new Hero[]{new Hero(100), new Hero(100)};
        assertThrows(IOException.class,
                () -> AreaMap.loadGame("testSave", "wrongUser", newHeroes),
                "Should throw IOException for wrong user");
    }

    @Test
    void testAutoSaveOnEndRound() throws IOException, ClassNotFoundException {
        logger.info("[AreaMapTest] testAutoSaveOnEndRound start");
        try {
            File saveDir = new File(tempDir.toFile(), "src/main/saves/testUser");
            assertTrue(saveDir.mkdirs(), "Save directory should be created");
            System.setProperty("user.dir", tempDir.toString());

            map.endRound(heroes);

            File[] saveFiles = saveDir.listFiles((_, name) -> name.startsWith("auto_save_end_round_") && name.endsWith(".sav"));
            assertNotNull(saveFiles, "Autosave directory should exist");
            assertEquals(1, saveFiles.length, "One autosave file should be created");

            Hero[] loadedHeroes = new Hero[]{new Hero(100), new Hero(100)};
            AreaMap loadedMap = AreaMap.loadGame(saveFiles[0].getName().replace(".sav", ""), "testUser", loadedHeroes);

            Props[][] loadedWorld = getWorld(loadedMap);
            assertInstanceOf(Hero.class, loadedWorld[5][1], "Player hero should be at (5,1) in autosave");
            assertInstanceOf(Hero.class, loadedWorld[5][8], "Bot hero should be at (5,8) in autosave");
            assertEquals(10, loadedHeroes[0].army.get("Lancer"), "Player hero army should be restored");
            assertEquals(6, loadedHeroes[1].army.get("Lancer"), "Bot hero army should be restored");
            logger.info("[AreaMapTest] testAutoSaveOnEndRound passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testAutoSaveOnEndRound failed", t);
            throw t;
        }
    }

    // Helper to access private world field (assumes getWorld() is not available)
    private Props[][] getWorld() {
        return getWorld(map);
    }

    private Props[][] getWorld(AreaMap targetMap) {
        try {
            java.lang.reflect.Field worldField = AreaMap.class.getDeclaredField("world");
            worldField.setAccessible(true);
            return (Props[][]) worldField.get(targetMap);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to access world field", e);
        }
    }

    @Test
    void testGetHeightWidth() {
        logger.info("[AreaMapTest] testGetHeightWidth start");
        try {
            assertEquals(height, map.getHeight());
            assertEquals(width, map.getWidth());
            logger.info("[AreaMapTest] testGetHeightWidth passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testGetHeightWidth failed", t);
            throw t;
        }
    }

    @Test
    void testApplySmoke_setsSmokeLayerWithinRadius() throws Exception {
        logger.info("[AreaMapTest] testApplySmoke_setsSmokeLayerWithinRadius start");
        try {
            int cy = 3, cx = 4;
            map.applySmoke(cy, cx);
            Field smokeField = AreaMap.class.getDeclaredField("smokeLayer");
            smokeField.setAccessible(true);
            boolean[][] smoke = (boolean[][]) smokeField.get(map);

            for (int i = cy - 2; i <= cy + 2; i++) {
                for (int j = cx - 2; j <= cx + 2; j++) {
                    assertTrue(smoke[i][j], "Should be smoky at (" + i + "," + j + ")");
                }
            }
            logger.info("[AreaMapTest] testApplySmoke_setsSmokeLayerWithinRadius passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testApplySmoke_setsSmokeLayerWithinRadius failed", t);
            throw t;
        }
    }

    @Test
    void testRemoveSmoke_clearsSmokeLayerAndResetsDisplay() throws Exception {
        logger.info("[AreaMapTest] testRemoveSmoke_clearsSmokeLayerAndResetsDisplay start");
        try {
            Hero[] heroes = {new Hero(0), new Hero(0)};
            map = new AreaMap(height, width, heroes, true);

            int cy = height / 2;
            int cx = width / 2;

            map.applySmoke(cy, cx);
            Field smokeField = AreaMap.class.getDeclaredField("smokeLayer");
            smokeField.setAccessible(true);
            boolean[][] smoke = (boolean[][]) smokeField.get(map);
            assertTrue(smoke[cy][cx], "Smoke layer should be true at center");

            map.removeSmoke(cy, cx);
            smoke = (boolean[][]) smokeField.get(map);
            assertFalse(smoke[cy][cx], "Smoke layer should be cleared");

            Field worldField = AreaMap.class.getDeclaredField("world");
            worldField.setAccessible(true);
            Props[][] world = (Props[][]) worldField.get(map);
            assertInstanceOf(Trail.class, world[cy][cx], "Original prop should remain");

            logger.info("[AreaMapTest] testRemoveSmoke_clearsSmokeLayerAndResetsDisplay passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testRemoveSmoke_clearsSmokeLayerAndResetsDisplay failed", t);
            throw t;
        }
    }

    @Test
    void testSmokeUnitSpawn_createsSmokeArea() {
        logger.info("[AreaMapTest] testSmokeUnitSpawn_createsSmokeArea start");
        try {
            Hero[] heroes = {new Hero(0), new Hero(0)};
            map = new AreaMap(height, width, heroes, true);

            SmokeUnit smokeUnit = new SmokeUnit(map);
            int centerY = smokeUnit.getSmokeCenterY();
            int centerX = smokeUnit.getSmokeCenterX();

            for (int i = centerY - 2; i <= centerY + 2; i++) {
                for (int j = centerX - 2; j <= centerX + 2; j++) {
                    if (i >= 0 && i < height && j >= 0 && j < width) {
                        assertTrue(map.isSmokeAt(i, j), "Smoke should be applied at (" + i + "," + j + ")");
                    }
                }
            }
            logger.info("[AreaMapTest] testSmokeUnitSpawn_createsSmokeArea passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testSmokeUnitSpawn_createsSmokeArea failed", t);
            throw t;
        }
    }

    @Test
    void testMoveThroughSmoke_reducesMovesCorrectly() throws Exception {
        logger.info("[AreaMapTest] testMoveThroughSmoke_reducesMovesCorrectly start");
        try {
            Hero h = new Hero(0);
            h.heroX = 1;
            h.heroY = 1;
            h.moves = 5;

            Field worldField = AreaMap.class.getDeclaredField("world");
            worldField.setAccessible(true);
            Props[][] world = (Props[][]) worldField.get(map);
            world[1][1] = h;
            world[1][2] = new Trail();

            map.applySmoke(1, 2);
            map.moveHero(h, 1, 0);

            assertEquals(2, h.heroX);
            assertEquals(1, h.heroY);
            assertEquals(4, h.moves);
            logger.info("[AreaMapTest] testMoveThroughSmoke_reducesMovesCorrectly passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testMoveThroughSmoke_reducesMovesCorrectly failed", t);
            throw t;
        }
    }

    @Test
    void testMoveHero_offMap_noChange() throws Exception {
        logger.info("[AreaMapTest] testMoveHero_offMap_noChange start");
        try {
            Hero h = new Hero(0);
            h.heroX = 0;
            h.heroY = 0;
            h.moves = 5;

            Field worldField = AreaMap.class.getDeclaredField("world");
            worldField.setAccessible(true);
            Props[][] world = (Props[][]) worldField.get(map);
            world[0][0] = h;

            map.moveHero(h, -1, 0);
            assertEquals(0, h.heroX);
            assertEquals(0, h.heroY);
            assertEquals(5, h.moves);
            logger.info("[AreaMapTest] testMoveHero_offMap_noChange passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testMoveHero_offMap_noChange failed", t);
            throw t;
        }
    }

    @Test
    void testMoveHero_trailMovement_reducesMovesAndMovesHero() throws Exception {
        logger.info("[AreaMapTest] testMoveHero_trailMovement_reducesMovesAndMovesHero start");
        try {
            Hero h = new Hero(0);
            h.heroX = 1;
            h.heroY = 1;
            h.moves = 3;

            Field worldField = AreaMap.class.getDeclaredField("world");
            worldField.setAccessible(true);
            Props[][] world = (Props[][]) worldField.get(map);
            world[1][1] = h;
            world[1][2] = new Trail();

            map.moveHero(h, 1, 0);
            assertEquals(2, h.heroX);
            assertEquals(1, h.heroY);
            assertEquals(2, h.moves);
            logger.info("[AreaMapTest] testMoveHero_trailMovement_reducesMovesAndMovesHero passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testMoveHero_trailMovement_reducesMovesAndMovesHero failed", t);
            throw t;
        }
    }

    @Test
    void testMoveHero_obstacle_noMove() throws Exception {
        logger.info("[AreaMapTest] testMoveHero_obstacle_noMove start");
        try {
            Hero h = new Hero(0);
            h.heroX = 1;
            h.heroY = 1;
            h.moves = 5;

            Field worldField = AreaMap.class.getDeclaredField("world");
            worldField.setAccessible(true);
            Props[][] world = (Props[][]) worldField.get(map);
            world[1][1] = h;
            world[1][2] = new Obstacle();

            map.moveHero(h, 1, 0);
            assertEquals(1, h.heroX);
            assertEquals(1, h.heroY);
            assertEquals(5, h.moves);
            logger.info("[AreaMapTest] testMoveHero_obstacle_noMove passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testMoveHero_obstacle_noMove failed", t);
            throw t;
        }
    }

    @Test
    void testEndGame_PlayerWinsByCapturingCastles() throws Exception {
        logger.info("[AreaMapTest] testEndGame_PlayerWinsByCapturingCastles start");
        try {
            Field worldField = AreaMap.class.getDeclaredField("world");
            worldField.setAccessible(true);
            Props[][] world = (Props[][]) worldField.get(map);
            world[height / 2][0] = new PlayerCastle();
            world[height / 2][width - 1] = new PlayerCastle();

            Method endGame = AreaMap.class.getDeclaredMethod("endGame");
            endGame.setAccessible(true);
            boolean result = (boolean) endGame.invoke(map);

            assertTrue(result, "The game must end in victory.");
            logger.info("[AreaMapTest] testEndGame_PlayerWinsByCapturingCastles passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testEndGame_PlayerWinsByCapturingCastles failed", t);
            throw t;
        }
    }

    @Test
    void testEndGame_PlayerLosesWhenBotCapturesCastles() throws Exception {
        logger.info("[AreaMapTest] testEndGame_PlayerLosesWhenBotCapturesCastles start");
        try {
            Field worldField = AreaMap.class.getDeclaredField("world");
            worldField.setAccessible(true);
            Props[][] world = (Props[][]) worldField.get(map);
            world[height / 2][0] = new BotCastle();
            world[height / 2][width - 1] = new BotCastle();

            Method endGame = AreaMap.class.getDeclaredMethod("endGame");
            endGame.setAccessible(true);
            boolean result = (boolean) endGame.invoke(map);

            assertTrue(result, "The game must end in defeat");
            logger.info("[AreaMapTest] testEndGame_PlayerLosesWhenBotCapturesCastles passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testEndGame_PlayerLosesWhenBotCapturesCastles failed", t);
            throw t;
        }
    }
}