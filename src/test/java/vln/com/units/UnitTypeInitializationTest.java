package vln.com.units;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.logging.*;

import static org.junit.jupiter.api.Assertions.*;

class UnitTypeInitializationTest {
    private static final Logger logger = Logger.getLogger(UnitTypeInitializationTest.class.getName());
    private static final String LOG_FILE = "src/test/logs/UnitTypeInitialization.log";

    static {
        try {
            FileHandler fh = new FileHandler(LOG_FILE, true);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setLevel(Level.ALL);
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Failed to initialize logger for UnitTypeInitializationTest", e);
        }
    }

    static Stream<Arguments> unitProvider() {
        int count = 3;
        return Stream.of(
                Arguments.of((Supplier<Unit>) () -> new Swordsman(count),
                        count, 10, 15, 7, 1, 3, "S"),
                Arguments.of((Supplier<Unit>) () -> new Archer(count),
                        count, 8, 5, 5, 100, 1, "A"),
                Arguments.of((Supplier<Unit>) () -> new Lancer(count),
                        count, 5, 7, 3, 1, 2, "L"),
                Arguments.of((Supplier<Unit>) () -> new Cavalryman(count),
                        count, 15, 12, 9, 2, 4, "C"),
                Arguments.of((Supplier<Unit>) () -> new Paladin(count),
                        count, 25, 18, 10, 2, 5, "P")
        );
    }

    @ParameterizedTest(name = "{index} → {7} x{1}")
    @MethodSource("unitProvider")
    void testUnitInitialization(Supplier<Unit> factory,
                                int count,
                                int expectedCost,
                                int expectedHP,
                                int expectedDamage,
                                int expectedDistance,
                                int expectedMovement,
                                String expectedDesign) {
        logger.info("[UnitTypeInitializationTest] testUnitInitialization start: " + expectedDesign + " x" + count);
        try {
            Unit u = factory.get();
            assertEquals(count, u.count, "Count should match constructor argument");
            assertEquals(expectedCost, u.cost, "Cost mismatch");
            assertEquals(expectedHP, u.HP, "HP mismatch");
            assertEquals(expectedDamage, u.damage, "Damage mismatch");
            assertEquals(expectedDistance, u.distance, "Distance mismatch");
            assertEquals(expectedMovement, u.movement, "Movement mismatch");
            assertEquals(expectedDesign, u.design, "Design symbol mismatch");
            assertEquals(expectedHP * count, u.stackHP, "Stack HP computed incorrectly");
            assertEquals(expectedDamage * count, u.stackDamage, "Stack damage computed incorrectly");
            logger.info("[UnitTypeInitializationTest] testUnitInitialization passed: " + expectedDesign + " x" + count);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testUnitInitialization failed: " + expectedDesign + " x" + count, t);
            throw t;
        }
    }
}
