package vln.com.que;

import vln.com.map.AreaMap;

import java.util.List;

public class RemoveQue extends Thread {
    private final List<String> list;
    private final AreaMap areaMap;

    public RemoveQue(List<String> list, AreaMap areaMap) {
        this.list = list;
        this.areaMap = areaMap;
    }

    //    @Override
//    public void run() {
//        while (!list.isEmpty()) {
//            synchronized (list) {
//                if (!list.isEmpty()) {
//                    ui.printNPC(list.remove(0));
//                }
//            }
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                return;
//            }
//        }
//        ui.endOfQue();
//    }
    @Override
    public void run() {
        while (!isInterrupted()) {
            synchronized (list) {
                while (list.isEmpty()) {
                    try {
                        list.wait(); // ждём, пока AddQue не добавит что-то
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                String item = list.remove(0);

                if ("END_OF_QUEUE".equals(item)) {
                    System.out.println("All people are served");
                    list.notify(); // На случай, если кто-то ждёт за нами
                    return;
                }

                try {
                    Thread.sleep(3000); // имитация времени обслуживания
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("An NPC came out - " + item);

            }
        }
    }
}