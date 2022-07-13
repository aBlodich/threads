package com.ablodich.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncMessageConsumer implements Runnable {
  private final BlockingQueue<Message> queue;
  private final Semaphore semaphore;
  private final List<Message> messages;

  public AsyncMessageConsumer(BlockingQueue<Message> queue, Semaphore semaphore) {
    this.queue = queue;
    this.semaphore = semaphore;
    this.messages = new ArrayList<>();
  }

  private void consume() throws InterruptedException {
    for(int i =0; i < 100; i++) {
      long start = System.currentTimeMillis();
      Message message = queue.poll(10L, TimeUnit.SECONDS);
      long end = System.currentTimeMillis() - start;
      if (message == null || end > 10000L) {
        log.info("Закончилось время ожидания, количество полученных сообщений: "
            + messages.size() + ". Время ожидания: " + end);
        break;
      }
      messages.add(message);
    }
  }

  @Override
  public void run() {
    ExecutorService pool = Executors.newFixedThreadPool(1);
    try {
      while (!Thread.currentThread().isInterrupted()) {
        semaphore.acquire();
        log.info("Thread: " + Thread.currentThread().getName() + " starts consuming");
        consume();
        semaphore.release();
        log.info("Thread: " + Thread.currentThread().getName() + " ends consuming");
        pool.execute(new MessageSender(new CopyOnWriteArrayList<>(messages)));
      }
    } catch (InterruptedException e) {
      log.info("interrupted");
    } finally {
      pool.shutdownNow();
    }
  }
}