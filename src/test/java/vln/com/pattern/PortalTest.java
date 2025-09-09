package vln.com.pattern;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.util.logging.*;

class PortalTest {
    private static final Logger logger = Logger.getLogger(PortalTest.class.getName());
    private static final String LOG_FILE = "src/test/logs/Portal.log";

    static {
        try {
            FileHandler fh = new FileHandler(LOG_FILE, true);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setLevel(Level.ALL);
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Failed to initialize logger for PortalTest", e);
        }
    }

    @Test
    void portalDesign_ShouldBeCorrect() {
        logger.info("[PortalTest] portalDesign_ShouldBeCorrect start");
        try {
            Portal portal = new Portal();
            assertEquals("\u001B[35m◎\u001B[0m", portal.design);
            logger.info("[PortalTest] portalDesign_ShouldBeCorrect passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "portalDesign_ShouldBeCorrect failed", t);
            throw t;
        }
    }

    @Test
    void turnsSinceSpawn_ShouldInitializeToZero() {
        logger.info("[PortalTest] turnsSinceSpawn_ShouldInitializeToZero start");
        try {
            Portal portal = new Portal();
            assertEquals(0, portal.turnsSinceSpawn);
            logger.info("[PortalTest] turnsSinceSpawn_ShouldInitializeToZero passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "turnsSinceSpawn_ShouldInitializeToZero failed", t);
            throw t;
        }
    }
}
