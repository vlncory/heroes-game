package vln.com.units;

import org.junit.jupiter.api.Test;
import java.util.logging.*;

import static org.junit.jupiter.api.Assertions.*;

class SwordsmanTest {
    private static final Logger logger = Logger.getLogger(SwordsmanTest.class.getName());
    private static final String LOG_FILE = "src/test/logs/Swordsman.log";

    static {
        try {
            FileHandler fh = new FileHandler(LOG_FILE, true);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setLevel(Level.ALL);
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Failed to initialize logger for SwordsmanTest", e);
        }
    }

    @Test
    void testSwordsmanBuffs_AppliedCorrectly() {
        logger.info("[SwordsmanTest] testSwordsmanBuffs_AppliedCorrectly start");
        try {
            int count = 5;

            Swordsman standard = new Swordsman(count, false, false);
            assertEquals(15, standard.HP, "Base HP should be 15");
            assertEquals(7, standard.damage, "Base damage should be 7");
            assertEquals(15 * count, standard.stackHP, "Stack HP without buffs is wrong");
            assertEquals(7 * count, standard.stackDamage, "Stack damage without buffs is wrong");

            Swordsman buffed = new Swordsman(count, true, true);
            assertEquals(16, buffed.HP, "Buffed HP should be 16");
            assertEquals(8, buffed.damage, "Buffed damage should be 8");
            assertEquals(16 * count, buffed.stackHP, "Stack HP with buffs is wrong (math order issue!)");
            assertEquals(8 * count, buffed.stackDamage, "Stack damage with buffs is wrong (math order issue!)");

            logger.info("[SwordsmanTest] testSwordsmanBuffs_AppliedCorrectly passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testSwordsmanBuffs_AppliedCorrectly failed", t);
            throw t;
        }
    }
}