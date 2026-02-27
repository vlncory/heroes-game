package vln.com.que;

public class SimpleQue extends Thread {
    @Override
    public synchronized void run() {
        try {
            sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}