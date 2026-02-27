package vln.com.que;

import java.util.List;

public class AddQue extends Thread {
    private final List<String> list;
    private final List<String> names;

    public AddQue(List<String> list, List<String> names) {
        this.list = list;
        this.names = names;
    }

    @Override
    public void run() {
        for (String name : names) {
            synchronized (list) {
                list.add(name);
                list.notify();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        synchronized (list) {
            list.add("END_OF_QUEUE");
            list.notify();
        }
    }
}