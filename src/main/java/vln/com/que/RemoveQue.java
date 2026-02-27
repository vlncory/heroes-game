package vln.com.que;

import java.util.List;

public class RemoveQue extends Thread {
    private final List<String> list;

    public RemoveQue(List<String> list) {
        this.list = list;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            String item;

            synchronized (list) {
                while (list.isEmpty()) {
                    try {
                        list.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                item = list.removeFirst();

                if ("END_OF_QUEUE".equals(item)) {
                    System.out.println("All people are served");
                    list.notify();
                    return;
                }
            }

            try {
                Thread.sleep(3000); // имитация времени обслуживания
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            System.out.println("An NPC came out - " + item);
        }
    }
}