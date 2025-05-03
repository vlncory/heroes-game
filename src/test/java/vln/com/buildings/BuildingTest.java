package vln.com.buildings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.logging.*;

import vln.com.units.Hero;

public class BuildingTest {
    private static final Logger logger = Logger.getLogger(BuildingTest.class.getName());
    private static final String LOG_FILE = "src/test/logs/Buildings.log";

    static {
        try {
            FileHandler fh = new FileHandler(LOG_FILE, true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            logger.setLevel(Level.ALL);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Logger init failed", e);
        }
    }

    private Building building;
    private Hero hero;

    @BeforeEach
    void setUp() {
        logger.info("[BuildingTest] Preparing test setup");
        building = new Building();
        hero = new Hero(100);
    }

    @Test
    void purchaseBuilding_ShouldDeductGoldAndMarkPurchased() {
        logger.info("[BuildingTest] Testing valid building purchase");
        try {
            building.handleBuildingPurchase(hero, "T");

            assertAll("Purchase validation",
                    () -> assertTrue(building.isBuildingPurchased("T")),
                    () -> assertEquals(85, hero.gold)
            );
            logger.info("[BuildingTest] Purchase validated");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Purchase test failed", e);
            throw e;
        }
    }

    @Test
    void stablePurchase_ShouldEnableHeroStable() {
        logger.info("[BuildingTest] Testing stable purchase");
        try {
            building.handleBuildingPurchase(hero, "S");
            assertTrue(hero.isStable);
            logger.info("[BuildingTest] Stable flag set");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Stable test failed", e);
            throw e;
        }
    }

    @Test
    void locatorPurchase_ShouldRevealSmokeWhenPlayer() {
        logger.info("[BuildingTest] Testing locator purchase");
        try {
            hero.isPlayer = true;
            building.handleBuildingPurchase(hero, "L");
            logger.info("[BuildingTest] Locator action completed");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Locator test failed", e);
            throw e;
        }
    }

    @Test
    void invalidPurchase_ShouldNotAffectState() {
        logger.info("[BuildingTest] Testing invalid purchase");
        try {
            building.handleBuildingPurchase(hero, "X");
            assertEquals(100, hero.gold);
            logger.info("[BuildingTest] State preserved");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Invalid purchase test failed", e);
            throw e;
        }
    }
}