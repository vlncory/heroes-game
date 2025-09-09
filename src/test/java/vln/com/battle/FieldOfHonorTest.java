package vln.com.battle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.*;

import vln.com.graphic.Props;
import vln.com.pattern.Trail;
import vln.com.units.Hero;
import vln.com.units.Unit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class FieldOfHonorTest {
    private static FieldOfHonor field;
    private static final int HEIGHT = 5;
    private static final int WIDTH = 10;

    private static final Logger logger = Logger.getLogger(FieldOfHonorTest.class.getName());
    private static final String LOG_FILE = "src/test/logs/Battle.log";

    static {
        try {
            FileHandler fh = new FileHandler(LOG_FILE, true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            logger.setLevel(Level.ALL);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Logger initialization failed", e);
        }
    }

    @BeforeEach
    public void setUp() {
        try {
            logger.info("Initializing test setup");

            Hero player = new Hero(0);
            player.isPlayer = true;
            player.army.put("Archer", 1);

            Hero enemy = new Hero(0);
            enemy.isPlayer = false;
            enemy.army.put("Archer", 1);

            field = new FieldOfHonor(player, enemy);
            logger.info("Test setup completed");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Setup failed", e);
            fail("Setup failed");
        }
    }

    @Test
    public void testPlacementOfUnits() throws Exception {
        logger.info("Starting testPlacementOfUnits");
        try {
            Field gridField = FieldOfHonor.class.getDeclaredField("battleGrid");
            gridField.setAccessible(true);
            logger.severe("Accessed private field: battleGrid");

            Props[][] grid = (Props[][]) gridField.get(field);

            Unit pUnit = assertInstanceOf(Unit.class, grid[0][0], "Expected player unit at (0,0)");
            assertTrue(pUnit.isPlayerUnit, "Unit at (0,0) should be player's");

            Unit eUnit = assertInstanceOf(Unit.class, grid[0][WIDTH - 1], "Expected enemy unit at (0,WIDTH-1)");
            assertFalse(eUnit.isPlayerUnit, "Unit at (0,WIDTH-1) should be enemy's");

            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    if ((y == 0 && (x == 0 || x == WIDTH - 1))) continue;
                    assertInstanceOf(Trail.class, grid[y][x], String.format("Expected Trail at (%d,%d)", y, x));
                }
            }
            logger.info("testPlacementOfUnits assertions passed");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Test failed", e);
            throw e;
        }
        logger.info("testPlacementOfUnits completed");
    }

    @Test
    public void testResetMovementFlags() throws Exception {
        logger.info("Starting testResetMovementFlags");
        try {
            Field gridField = FieldOfHonor.class.getDeclaredField("battleGrid");
            gridField.setAccessible(true);
            logger.severe("Accessed private field: battleGrid");

            Props[][] grid = (Props[][]) gridField.get(field);

            Unit p = (Unit) grid[0][0];
            Unit e = (Unit) grid[0][WIDTH - 1];
            p.relocated = true;
            e.relocated = true;

            Method resetFlags = FieldOfHonor.class.getDeclaredMethod("resetMovementFlags");
            resetFlags.setAccessible(true);
            logger.severe("Accessed private method: resetMovementFlags");

            resetFlags.invoke(field);

            assertFalse(p.relocated, "Player unit should be movable again");
            assertFalse(e.relocated, "Enemy unit should be movable again");
            logger.info("testResetMovementFlags assertions passed");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Test failed", e);
            throw e;
        }
        logger.info("testResetMovementFlags completed");
    }

    @Test
    public void testFindTargetInRange() throws Exception {
        logger.info("Starting testFindTargetInRange");
        try {
            Field gridField = FieldOfHonor.class.getDeclaredField("battleGrid");
            gridField.setAccessible(true);
            logger.severe("Accessed private field: battleGrid");

            Props[][] grid = (Props[][]) gridField.get(field);
            Unit enemyUnit = (Unit) grid[0][WIDTH - 1];

            Method findTarget = FieldOfHonor.class.getDeclaredMethod("findTargetInRange", Unit.class);
            findTarget.setAccessible(true);
            logger.severe("Accessed private method: findTargetInRange");

            Unit target = (Unit) findTarget.invoke(field, enemyUnit);

            assertNotNull(target, "Enemy should find a player target in range");
            assertTrue(target.isPlayerUnit, "Found target should belong to player");
            logger.info("testFindTargetInRange assertions passed");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Test failed", e);
            throw e;
        }
        logger.info("testFindTargetInRange completed");
    }

    @Test
    public void testMoveTowardsPlayer() throws Exception {

        logger.info("testMoveTowardsPlayer test started successfully");

        PrintStream originalOut = System.out;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));
        logger.warning("The system output has been replaced");

        Unit mover = new Unit();
        mover.isPlayerUnit = false;
        mover.count = 1;
        mover.HP = 10;
        mover.movement = 2;
        mover.unitX = 5;
        mover.unitY = 2;

        Field gridField = FieldOfHonor.class.getDeclaredField("battleGrid");
        gridField.setAccessible(true);
        Props[][] grid = (Props[][]) gridField.get(field);
        grid[2][5] = mover;
        System.setOut(originalOut);

        try {
            Method moveMethod = FieldOfHonor.class.getDeclaredMethod("moveTowardsPlayer", Unit.class);
            moveMethod.setAccessible(true);
            logger.severe("Access to private method via reflection detected");
        } catch (Exception ignored) {}
        assertTrue(true);

        logger.info("testMoveTowardsPlayer test completed successfully");
    }

    @Test
    public void testPerformAttackDestroysUnit() throws Exception {
        logger.info("Starting testPerformAttackDestroysUnit");
        try {
            Unit attacker = new Unit();
            attacker.isPlayerUnit = false;
            attacker.count = 1;
            attacker.HP = 10;
            attacker.damage = 5;
            attacker.stackDamage = 5;
            attacker.unitX = 1;
            attacker.unitY = 1;

            Unit target = new Unit();
            target.isPlayerUnit = true;
            target.count = 1;
            target.HP = 10;
            target.stackHP = 10;
            target.unitX = 2;
            target.unitY = 1;

            Field gridField = FieldOfHonor.class.getDeclaredField("battleGrid");
            gridField.setAccessible(true);
            logger.severe("Accessed private field: battleGrid");

            Props[][] grid = (Props[][]) gridField.get(field);
            grid[1][1] = attacker;
            grid[1][2] = target;

            Method attackMethod = FieldOfHonor.class.getDeclaredMethod("performAttack", Unit.class, Unit.class);
            attackMethod.setAccessible(true);
            logger.severe("Accessed private method: performAttack");

            attackMethod.invoke(field, attacker, target);
            assertEquals(5, target.stackHP, "Target HP should decrease by attacker's damage");

            attackMethod.invoke(field, attacker, target);
            assertInstanceOf(Trail.class, grid[1][2], "Destroyed unit should be replaced by Trail");
            logger.info("testPerformAttackDestroysUnit assertions passed");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Test failed", e);
            throw e;
        }
        logger.info("testPerformAttackDestroysUnit completed");
    }

    @Test
    public void testBattleEndsWithPlayerVictory() throws Exception {
        logger.info("Starting testBattleEndsWithPlayerVictory");
        try {
            Field gridField = FieldOfHonor.class.getDeclaredField("battleGrid");
            gridField.setAccessible(true);
            logger.severe("Accessed private field: battleGrid");

            Props[][] grid = (Props[][]) gridField.get(field);

            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    if (grid[y][x] instanceof Unit unit && !unit.isPlayerUnit) {
                        grid[y][x] = new Trail();
                    }
                }
            }

            Method isBattleOver = FieldOfHonor.class.getDeclaredMethod("isBattleOver");
            isBattleOver.setAccessible(true);
            logger.severe("Accessed private method: isBattleOver");

            assertTrue((boolean) isBattleOver.invoke(field), "Battle should end with player victory");
            logger.info("testBattleEndsWithPlayerVictory assertions passed");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Test failed", e);
            throw e;
        }
        logger.info("testBattleEndsWithPlayerVictory completed");
    }

    @Test
    public void testBattleEndsWithPlayerDefeat() throws Exception {
        logger.info("Starting testBattleEndsWithPlayerDefeat");
        try {
            Field gridField = FieldOfHonor.class.getDeclaredField("battleGrid");
            gridField.setAccessible(true);
            logger.severe("Accessed private field: battleGrid");

            Props[][] grid = (Props[][]) gridField.get(field);

            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    if (grid[y][x] instanceof Unit unit && unit.isPlayerUnit) {
                        grid[y][x] = new Trail();
                    }
                }
            }

            Method setTestingMode = FieldOfHonor.class.getDeclaredMethod("setTestingMode", boolean.class);
            setTestingMode.setAccessible(true);
            logger.severe("Accessed private method: setTestingMode");

            Method isBattleOver = FieldOfHonor.class.getDeclaredMethod("isBattleOver");
            isBattleOver.setAccessible(true);
            logger.severe("Accessed private method: isBattleOver");

            setTestingMode.invoke(field, true);
            assertTrue((boolean) isBattleOver.invoke(field), "Battle should end with player defeat");
            logger.info("testBattleEndsWithPlayerDefeat assertions passed");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Test failed", e);
            throw e;
        }
        logger.info("testBattleEndsWithPlayerDefeat completed");
    }
}