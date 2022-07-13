package com.ablodich.threads;

import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
  private static final Logger log = LoggerFactory.getLogger(Main.class);
  private final static int PRODUCERS_COUNT = 10;
  private final static int CONSUMERS_COUNT = 10;

  public static void main(String[] args) {
    System.out.println("Нажмите любую клавишу для завершения работы программы и нажмите enter.");
    System.out.println("Программа запустится через 5с.");

    try {
      Thread.sleep(5000L);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    BlockingQueue<Message> queue = new DelayQueue<>();
    CyclicBarrier barrier = new CyclicBarrier(PRODUCERS_COUNT);

    Thread[] producers = new Thread[PRODUCERS_COUNT];

    for (int i = 0; i < PRODUCERS_COUNT; i++) {
      producers[i] = new Thread(new MessageProducer(queue, barrier));
      producers[i].start();
    }

    Thread[] consumers = new Thread[CONSUMERS_COUNT];
    Semaphore semaphore = new Semaphore(1, true);

    for (int i = 0; i < CONSUMERS_COUNT; i++) {
      consumers[i] = new Thread(new MessageConsumer(queue, semaphore));
      consumers[i].start();
    }

    BlockingQueue<Message> queue2 = new DelayQueue<>();
    CyclicBarrier barrier2 = new CyclicBarrier(PRODUCERS_COUNT);
    ExecutorService producersExecutorService = Executors.newFixedThreadPool(PRODUCERS_COUNT);
    for (int i = 0; i < PRODUCERS_COUNT; i++) {
      producersExecutorService.execute(new MessageProducer(queue2, barrier2));
    }

    ExecutorService consumersExecutorService = Executors.newFixedThreadPool(CONSUMERS_COUNT);
    Semaphore semaphore2 = new Semaphore(1, true);
    for (int i = 0; i < CONSUMERS_COUNT; i++) {
      consumersExecutorService.execute(new AsyncMessageConsumer(queue2, semaphore2));
    }

    Scanner sc = new Scanner(System.in);
    sc.nextLine();

    for (int i = 0; i < CONSUMERS_COUNT; i++) {
      consumers[i].interrupt();
    }
    for (int i = 0; i < PRODUCERS_COUNT; i++) {
      producers[i].interrupt();
    }

    consumersExecutorService.shutdownNow();
    producersExecutorService.shutdownNow();

    try {
      boolean cons = consumersExecutorService.awaitTermination(10, TimeUnit.SECONDS);
      log.info("Завершение потоков потребилетей в пуле: " + cons);
      boolean prods = producersExecutorService.awaitTermination(110, TimeUnit.SECONDS);
      log.info("Завершение потоков производителей в пуле: " + prods);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}