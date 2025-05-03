package vln.com.units;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.Set;
import java.util.logging.*;

import static org.junit.jupiter.api.Assertions.*;

import vln.com.buildings.Building;

class HeroTest {
    private static final Logger logger = Logger.getLogger(HeroTest.class.getName());
    private static final String LOG_FILE = "src/test/logs/Hero.log";

    static {
        try {
            FileHandler fh = new FileHandler(LOG_FILE, true);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setLevel(Level.ALL);
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Failed to initialize logger for HeroTest", e);
        }
    }

    private Hero hero;
    private MockBuilding castleFull;
    private MockBuilding castleEmpty;

    static class MockBuilding extends Building {
        private final Set<String> purchased;
        MockBuilding(Set<String> purchased) {
            this.purchased = purchased;
        }
        @Override
        public boolean isBuildingPurchased(String code) {
            return purchased.contains(code);
        }
    }

    @BeforeEach
    void init() {
        logger.info("[HeroTest] init start");
        hero = new Hero(1000);
        hero.army.clear();
        hero.isStable = false;
        hero.moves = 0;

        castleEmpty = new MockBuilding(Collections.emptySet());
        Set<String> full = Set.of("G", "T", "A", "W", "AA", "C");
        castleFull = new MockBuilding(full);
        logger.info("[HeroTest] init completed");
    }

    @Test
    void testSetSymbol() {
        logger.info("[HeroTest] testSetSymbol start");
        try {
            hero.setSymbol("H");
            assertEquals("H", hero.design);
            logger.info("[HeroTest] testSetSymbol passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testSetSymbol failed", t);
            throw t;
        }
    }

    @Test
    void testResetMoves_whenNotStable() {
        logger.info("[HeroTest] testResetMoves_whenNotStable start");
        try {
            hero.isStable = false;
            hero.resetMoves();
            assertEquals(10, hero.moves, "When not stable hero should have 10 moves");
            logger.info("[HeroTest] testResetMoves_whenNotStable passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testResetMoves_whenNotStable failed", t);
            throw t;
        }
    }

    @Test
    void testResetMoves_whenStable() {
        logger.info("[HeroTest] testResetMoves_whenStable start");
        try {
            hero.isStable = true;
            hero.resetMoves();
            assertEquals(15, hero.moves, "When stable hero should have 15 moves");
            logger.info("[HeroTest] testResetMoves_whenStable passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testResetMoves_whenStable failed", t);
            throw t;
        }
    }

    @Test
    void testUnitPurchase_invalidChoice_noChange() {
        logger.info("[HeroTest] testUnitPurchase_invalidChoice_noChange start");
        try {
            hero.gold = 500;
            hero.unitPurchase(castleFull, "X", hero.gold);
            assertTrue(hero.army.isEmpty(), "Army must remain empty on invalid choice");
            assertEquals(500, hero.gold, "Gold should not change on invalid choice");
            logger.info("[HeroTest] testUnitPurchase_invalidChoice_noChange passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testUnitPurchase_invalidChoice_noChange failed", t);
            throw t;
        }
    }

    @Test
    void testUnitPurchase_withoutPrerequisites_noPurchase() {
        logger.info("[HeroTest] testUnitPurchase_withoutPrerequisites_noPurchase start");
        try {
            hero.gold = 500;
            hero.unitPurchase(castleEmpty, "1", hero.gold);
            assertTrue(hero.army.isEmpty(), "Army empty when missing Guard post or Tavern");
            assertEquals(500, hero.gold, "Gold unchanged when prerequisites missing");
            logger.info("[HeroTest] testUnitPurchase_withoutPrerequisites_noPurchase passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testUnitPurchase_withoutPrerequisites_noPurchase failed", t);
            throw t;
        }
    }

    @Test
    void testUnitPurchase_buyLancer() {
        logger.info("[HeroTest] testUnitPurchase_buyLancer start");
        try {
            Lancer sample = new Lancer(0);
            int cost = sample.cost;
            int qty = 3;
            hero = new Hero(cost * qty);
            hero.army.clear();

            hero.unitPurchase(castleFull, "1", hero.gold);
            assertEquals(qty, hero.army.get("Lancer"));
            assertEquals(0, hero.gold);
            logger.info("[HeroTest] testUnitPurchase_buyLancer passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testUnitPurchase_buyLancer failed", t);
            throw t;
        }
    }

    @Test
    void testUnitPurchase_buyArcher_partialGoldRemaining() {
        logger.info("[HeroTest] testUnitPurchase_buyArcher_partialGoldRemaining start");
        try {
            Archer sample = new Archer(0);
            int cost = sample.cost;
            int qty = 2;
            hero = new Hero(cost * qty + cost / 2);
            hero.army.clear();

            hero.unitPurchase(castleFull, "2", hero.gold);
            assertEquals(qty, hero.army.get("Archer"));
            assertEquals(cost / 2, hero.gold);
            logger.info("[HeroTest] testUnitPurchase_buyArcher_partialGoldRemaining passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testUnitPurchase_buyArcher_partialGoldRemaining failed", t);
            throw t;
        }
    }

    @Test
    void testUnitPurchase_buySwordsman_exactGold() {
        logger.info("[HeroTest] testUnitPurchase_buySwordsman_exactGold start");
        try {
            Swordsman sample = new Swordsman(0);
            int cost = sample.cost;
            hero = new Hero(cost);
            hero.army.clear();

            hero.unitPurchase(castleFull, "3", hero.gold);
            assertEquals(1, hero.army.get("Swordsman"));
            assertEquals(0, hero.gold);
            logger.info("[HeroTest] testUnitPurchase_buySwordsman_exactGold passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testUnitPurchase_buySwordsman_exactGold failed", t);
            throw t;
        }
    }

    @Test
    void testUnitPurchase_buyCavalryman_zeroIfInsufficient() {
        logger.info("[HeroTest] testUnitPurchase_buyCavalryman_zeroIfInsufficient start");
        try {
            Cavalryman sample = new Cavalryman(0);
            int cost = sample.cost;
            hero = new Hero(cost - 1);
            hero.army.clear();

            hero.unitPurchase(castleFull, "4", hero.gold);
            assertFalse(hero.army.containsKey("Cavalryman"));
            assertEquals(cost - 1, hero.gold);
            logger.info("[HeroTest] testUnitPurchase_buyCavalryman_zeroIfInsufficient passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testUnitPurchase_buyCavalryman_zeroIfInsufficient failed", t);
            throw t;
        }
    }

    @Test
    void testUnitPurchase_buyPaladin_multiple() {
        logger.info("[HeroTest] testUnitPurchase_buyPaladin_multiple start");
        try {
            Paladin sample = new Paladin(0);
            int cost = sample.cost;
            int qty = 5;
            hero = new Hero(cost * qty * 2);
            hero.army.clear();

            hero.unitPurchase(castleFull, "5", hero.gold);
            assertEquals(qty * 2, hero.army.get("Paladin"));
            assertEquals(0, hero.gold);
            logger.info("[HeroTest] testUnitPurchase_buyPaladin_multiple passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testUnitPurchase_buyPaladin_multiple failed", t);
            throw t;
        }
    }
}
