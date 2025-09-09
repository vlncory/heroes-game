package vln.com.pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.*;

import static org.junit.jupiter.api.Assertions.*;

import vln.com.buildings.BotCastle;
import vln.com.buildings.PlayerCastle;
import vln.com.graphic.Props;
import vln.com.units.Hero;
import vln.com.units.SmokeUnit;

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
    private final int height = 7;
    private final int width = 9;

    @BeforeEach
    void setUp() {
        logger.info("[AreaMapTest] Setting up AreaMap instance");
        map = new AreaMap(height, width, new Hero[0], false);
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
    void testHasAnyMoves_trailAndFieldCosts() throws Exception {
        logger.info("[AreaMapTest] testHasAnyMoves_trailAndFieldCosts start");
        try {
            Hero h = new Hero(0);
            h.heroX = 1;
            h.heroY = 1;
            h.moves = 2;

            Field worldField = AreaMap.class.getDeclaredField("world");
            worldField.setAccessible(true);
            Props[][] world = (Props[][]) worldField.get(map);
            world[1][1] = h;
            world[1][2] = new Trail();

            assertTrue(map.hasAnyMoves(h));

            world[1][2] = null;
            h.moves = 1;
            assertFalse(map.hasAnyMoves(h));
            logger.info("[AreaMapTest] testHasAnyMoves_trailAndFieldCosts passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testHasAnyMoves_trailAndFieldCosts failed", t);
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
