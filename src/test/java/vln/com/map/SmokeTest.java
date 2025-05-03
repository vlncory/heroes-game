package vln.com.map;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.util.logging.*;

import vln.com.graphic.Props;

class SmokeTest {
    private static final Logger logger = Logger.getLogger(SmokeTest.class.getName());
    private static final String LOG_FILE = "src/test/logs/Smoke.log";

    static {
        try {
            FileHandler fh = new FileHandler(LOG_FILE, true);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setLevel(Level.ALL);
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Failed to initialize logger for SmokeTest", e);
        }
    }

    @Test
    void smokeDesign_ShouldBeCorrect() {
        logger.info("[SmokeTest] smokeDesign_ShouldBeCorrect start");
        try {
            Props original = new Props();
            Smoke smoke = new Smoke(original);

            assertEquals("\u001B[37m▒\u001B[0m", smoke.design);
            logger.info("[SmokeTest] smokeDesign_ShouldBeCorrect passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "smokeDesign_ShouldBeCorrect failed", t);
            throw t;
        }
    }

    @Test
    void originalProp_ShouldBeStoredCorrectly() {
        logger.info("[SmokeTest] originalProp_ShouldBeStoredCorrectly start");
        try {
            Props original = new Props();
            Smoke smoke = new Smoke(original);

            assertSame(original, smoke.originalProp);
            logger.info("[SmokeTest] originalProp_ShouldBeStoredCorrectly passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "originalProp_ShouldBeStoredCorrectly failed", t);
            throw t;
        }
    }
}
