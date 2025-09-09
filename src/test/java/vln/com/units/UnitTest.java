package vln.com.units;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.logging.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import vln.com.graphic.Props;
import vln.com.pattern.Trail;

class UnitTest {
    private static final Logger logger = Logger.getLogger(UnitTest.class.getName());
    private static final String LOG_FILE = "src/test/logs/Unit.log";

    static {
        try {
            FileHandler fh = new FileHandler(LOG_FILE, true);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setLevel(Level.ALL);
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Failed to initialize logger for UnitTest", e);
        }
    }

    private Unit unit;
    private Props[][] battlefield;

    @BeforeEach
    public void setUp() {
        logger.info("[UnitTest] setUp start");
        battlefield = new Props[5][5];
        for (int y = 0; y < battlefield.length; y++) {
            for (int x = 0; x < battlefield[0].length; x++) {
                battlefield[y][x] = new Trail();
            }
        }

        unit = new Unit();
        unit.HP = 10;
        unit.damage = 4;
        unit.count = 3;
        unit.stackHP = unit.count * unit.HP;
        unit.update();
        unit.distance = 1;
        unit.movement = 2;
        unit.unitX = 2;
        unit.unitY = 2;
        unit.isPlayerUnit = true;

        battlefield[unit.unitY][unit.unitX] = unit;
        logger.info("[UnitTest] setUp completed");
    }

    @Test
    public void testUpdate_computesStackDamageCorrectly() {
        logger.info("[UnitTest] testUpdate_computesStackDamageCorrectly start");
        try {
            assertEquals(12, unit.stackDamage);

            unit.count = 5;
            unit.update();
            assertEquals(20, unit.stackDamage);
            logger.info("[UnitTest] testUpdate_computesStackDamageCorrectly passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testUpdate_computesStackDamageCorrectly failed", t);
            throw t;
        }
    }

    @Test
    public void testCasualties_exactFullUnitLoss() {
        logger.info("[UnitTest] testCasualties_exactFullUnitLoss start");
        try {
            unit.casualties(unit.HP);
            assertEquals(2, unit.count);
            assertEquals(2 * unit.HP, unit.stackHP);
            assertEquals(2 * unit.damage, unit.stackDamage);
            logger.info("[UnitTest] testCasualties_exactFullUnitLoss passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testCasualties_exactFullUnitLoss failed", t);
            throw t;
        }
    }

    @Test
    public void testCasualties_partialDamageNotLosingUnit() {
        logger.info("[UnitTest] testCasualties_partialDamageNotLosingUnit start");
        try {
            int dmg = unit.HP / 2;
            unit.casualties(dmg);
            assertEquals(3, unit.count);
            assertEquals(3 * unit.HP - dmg, unit.stackHP);
            assertEquals(3 * unit.damage, unit.stackDamage);
            logger.info("[UnitTest] testCasualties_partialDamageNotLosingUnit passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testCasualties_partialDamageNotLosingUnit failed", t);
            throw t;
        }
    }

    @Test
    public void testFindUnits_noEnemiesInRange() throws Exception {
        logger.info("[UnitTest] testFindUnits_noEnemiesInRange start");
        try {
            Method findUnits = Unit.class.getDeclaredMethod("findUnits", Props[][].class);
            findUnits.setAccessible(true);

            boolean result = (boolean) findUnits.invoke(unit, (Object) battlefield);
            assertFalse(result);
            logger.info("[UnitTest] testFindUnits_noEnemiesInRange passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testFindUnits_noEnemiesInRange failed", t);
            throw t;
        }
    }

    @Test
    public void testFindUnits_enemyWithinRange() throws Exception {
        logger.info("[UnitTest] testFindUnits_enemyWithinRange start");
        try {
            Unit enemy = new Unit();
            enemy.isPlayerUnit = false;
            enemy.unitX = 3;
            enemy.unitY = 2;
            battlefield[2][3] = enemy;

            Method findUnits = Unit.class.getDeclaredMethod("findUnits", Props[][].class);
            findUnits.setAccessible(true);

            boolean result = (boolean) findUnits.invoke(unit, (Object) battlefield);
            assertTrue(result);
            logger.info("[UnitTest] testFindUnits_enemyWithinRange passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testFindUnits_enemyWithinRange failed", t);
            throw t;
        }
    }

    @Test
    public void testFindUnits_ignoreFriendlyUnits() throws Exception {
        logger.info("[UnitTest] testFindUnits_ignoreFriendlyUnits start");
        try {
            Unit friend = new Unit();
            friend.isPlayerUnit = true;
            friend.unitX = 2;
            friend.unitY = 3;
            battlefield[3][2] = friend;

            Method findUnits = Unit.class.getDeclaredMethod("findUnits", Props[][].class);
            findUnits.setAccessible(true);

            boolean result = (boolean) findUnits.invoke(unit, (Object) battlefield);
            assertFalse(result);
            logger.info("[UnitTest] testFindUnits_ignoreFriendlyUnits passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testFindUnits_ignoreFriendlyUnits failed", t);
            throw t;
        }
    }

    @Test
    public void testMoveUnit_movesWithinBounds() throws Exception {
        logger.info("[UnitTest] testMoveUnit_movesWithinBounds start");
        try {
            Method moveUnit = Unit.class.getDeclaredMethod("moveUnit", Props[][].class, String.class);
            moveUnit.setAccessible(true);

            moveUnit.invoke(unit, battlefield, "W");
            assertTrue(unit.unitY <= 2);
            assertTrue(unit.relocated);
            assertEquals(unit, battlefield[unit.unitY][unit.unitX]);
            logger.info("[UnitTest] testMoveUnit_movesWithinBounds passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testMoveUnit_movesWithinBounds failed", t);
            throw t;
        }
    }
}
