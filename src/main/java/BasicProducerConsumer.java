import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

/**
 * Created by smas on 27/1/2017.
 */
public class BasicProducerConsumer {

    private static List<Item> buffer = new ArrayList<Item>();

    private static int bufferSize = 10_000;
    private static int numItemsToAdd = 1_000_000;
    private static int numConsumed = 0;

    private static Object lock = new Object();



    public static void main(String arg[]) throws InterruptedException {

        Runnable p = () -> {
            Producer producer = new Producer();

            for(int i = 0; i < numItemsToAdd; i++) {
                producer.add(new Item());
            }
        };

        Runnable c = () -> {
            Consumer consumer = new Consumer();

            for(int i = 0; i < numItemsToAdd; i++) {
                consumer.take();
            }
        };

        Thread threadP = new Thread(p);
        threadP.start();

        Thread threadC = new Thread(c);
        threadC.start();

        threadP.join();
        threadC.join();

        System.out.println(numConsumed);

    }


    static class Producer {

        public void add(Item item) {

            synchronized (lock) {
                while(buffer.size() == bufferSize) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {}
                }
                buffer.add(item);
                lock.notify();
            }
        }
    }


    static class Consumer {

        public Item take() {
            synchronized (lock) {
                while (buffer.size() == 0) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {}
                }
                Item i = buffer.remove(0);
                lock.notify();
                numConsumed++;
                return i;
            }
        }
    }

    static class Item {}

}
