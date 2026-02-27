package vln.com.que;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.*;

import static org.junit.jupiter.api.Assertions.*;

class QueuesTest {
    private static final Logger logger = Logger.getLogger(QueuesTest.class.getName());
    private static final String LOG_FILE = "src/test/logs/Queues.log";

    static {
        try {
            FileHandler fh = new FileHandler(LOG_FILE, true);
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setLevel(Level.ALL);
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Failed to initialize logger for QueuesTest", e);
        }
    }

    @Test
    void testAddQue_fillsListAndAddsEndOfQueue() {
        logger.info("[QueuesTest] testAddQue_fillsListAndAddsEndOfQueue start");
        try {
            List<String> list = Collections.synchronizedList(new ArrayList<>());
            List<String> names = List.of("Alice", "Bob");

            AddQue addQue = new AddQue(list, names);
            addQue.start();
            addQue.join();

            assertEquals(3, list.size(), "List should contain 2 names + END_OF_QUEUE");
            assertEquals("Alice", list.get(0));
            assertEquals("Bob", list.get(1));
            assertEquals("END_OF_QUEUE", list.get(2), "Last element must be END_OF_QUEUE");

            logger.info("[QueuesTest] testAddQue_fillsListAndAddsEndOfQueue passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testAddQue_fillsListAndAddsEndOfQueue failed", t);
            throw new RuntimeException(t);
        }
    }

    @Test
    void testRemoveQue_processesItemsAndStopsOnEnd() {
        logger.info("[QueuesTest] testRemoveQue_processesItemsAndStopsOnEnd start");
        try {
            List<String> list = Collections.synchronizedList(new ArrayList<>());
            list.add("Alice");
            list.add("END_OF_QUEUE");

            RemoveQue removeQue = new RemoveQue(list);

            removeQue.start();

            removeQue.join(5000);

            assertFalse(removeQue.isAlive(), "RemoveQue thread should have terminated after END_OF_QUEUE");
            assertTrue(list.isEmpty(), "List should be empty after processing");

            logger.info("[QueuesTest] testRemoveQue_processesItemsAndStopsOnEnd passed");
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "testRemoveQue_processesItemsAndStopsOnEnd failed", t);
            throw new RuntimeException(t);
        }
    }
}