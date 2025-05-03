package vln.com.buildings;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.logging.*;

public class BotCastleTest {
    private static final Logger logger = Logger.getLogger(BotCastleTest.class.getName());
    private static final String LOG_FILE = "src/test/logs/BotCastle.log";

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
    void botCastleInitialization_ShouldHaveCorrectParameters() {
        logger.info("[BotCastle] Test started");

        try {
            BotCastle castle = new BotCastle();
            logger.info("Instance created");

            // Основные проверки
            assertAll("BotCastle properties",
                    () -> assertFalse(castle.isPlayerTower),
                    () -> assertEquals("\u001B[31m⏏\u001B[0m", castle.design),
                    () -> assertNotNull(castle.aegis),
                    () -> assertEquals(4, castle.aegis.army.get("Archer"))
            );

            logger.info("[BotCastle] All assertions passed");
        } catch (AssertionError | Exception e) {
            logger.log(Level.SEVERE, "Test failed: " + e.getMessage(), e);
            throw e;
        }

        logger.info("[BotCastle] Test completed\n");
    }
}