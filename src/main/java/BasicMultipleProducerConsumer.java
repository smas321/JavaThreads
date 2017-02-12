import java.util.ArrayList;
import java.util.List;

/**
 * Created by smas on 27/1/2017.
 */
public class BasicMultipleProducerConsumer {

    private static List<Item> buffer = new ArrayList<Item>();

    private static int bufferSize = 1000;
    private static int numItemsToAdd = 1_000_000;

    private static int numMsgProduced = 0;
    private static int numMsgConsumed = 0;

    private static Object lock = new Object();

    private static int numConsumers = 20;
    private static int numProducers = 20;


    public static void main(String arg[]) throws InterruptedException {


        List<Thread> consumers = new ArrayList<>(numConsumers);
        for(int i = 0; i < numConsumers; i++) {
            Runnable c = () -> {
                Consumer consumer = new Consumer();

                synchronized (lock) {
                    while(numMsgConsumed < numItemsToAdd) {
                        consumer.take();
                    }
                }


            };

            Thread threadC = new Thread(c);
            threadC.setName("Consumer_" + i);
            consumers.add(threadC);

            threadC.start();
        }

        List<Thread> producers = new ArrayList<>(numProducers);
        for(int i = 0; i < numProducers; i++) {
            Runnable p = () -> {
                Producer producer = new Producer();

                synchronized (lock) {
                    while(numMsgProduced < numItemsToAdd) {
                        producer.add(new Item());
                    }
                }
            };

            Thread threadP = new Thread(p);
            threadP.setName("Producer_" + i);
            threadP.start();
        }




        for(Thread c: consumers) {
            c.join();
        }

        for(Thread p: producers) {
            p.join();
        }

        System.out.println("Number produced " + numMsgProduced);
        System.out.println("Number consumed " + numMsgConsumed);
    }


    static class Producer {

        public void add(Item item) {

            while(buffer.size() == bufferSize) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {}

                if(numMsgProduced == numItemsToAdd) {
                    return;
                }
            }


            buffer.add(item);
            ++numMsgProduced;
            lock.notifyAll();
        }
    }


    static class Consumer {

        public void take() {

            while (buffer.size() == 0) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {}

                if(numMsgConsumed == numItemsToAdd) {
                    return;
                }
            }
            buffer.remove(0);
            ++numMsgConsumed;
            lock.notifyAll();
        }
    }

    static class Item {}

}
