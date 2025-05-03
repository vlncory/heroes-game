package vln.com.buildings;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.logging.*;

public class PlayerCastleTest {
    private static final Logger logger = Logger.getLogger(PlayerCastleTest.class.getName());
    private static final String LOG_FILE = "src/test/logs/PlayerCastle.log";

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

    @Test
    void playerCastleInitialization_ShouldHaveCorrectParameters() {
        logger.info("[PlayerCastle] Test started");

        try {
            PlayerCastle castle = new PlayerCastle();
            logger.info("Instance created");

            assertAll("Castle properties",
                    () -> assertTrue(castle.isPlayerTower),
                    () -> assertEquals("\u001B[34m⏏\u001B[0m", castle.design),
                    () -> assertNotNull(castle.aegis),
                    () -> assertEquals(4, castle.aegis.army.get("Archer"))
            );

            logger.info("[PlayerCastle] All properties validated");
        } catch (AssertionError | Exception e) {
            logger.log(Level.SEVERE, "Test failed: " + e.getMessage(), e);
            throw e;
        }

        logger.info("[PlayerCastle] Test completed\n");
    }
}