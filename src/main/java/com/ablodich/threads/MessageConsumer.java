package com.ablodich.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageConsumer implements Runnable {
  private final BlockingQueue<Message> queue;
  private final Semaphore semaphore;
  private final List<Message> messages;

  public MessageConsumer(BlockingQueue<Message> queue, Semaphore semaphore) {
    this.queue = queue;
    this.semaphore = semaphore;
    this.messages = new ArrayList<>();
  }

  @Override
  public void run() {
    try {
      while (!Thread.currentThread().isInterrupted()) {
        semaphore.acquire();
        log.info("Thread: " + Thread.currentThread().getName() + " starts consuming");
        consume();
        semaphore.release();
        log.info("Thread: " + Thread.currentThread().getName() + " ends consuming");
        sendMessages(messages);
      }
    } catch (InterruptedException e) {
      log.info("interrupted");
    }
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

  private void sendMessages(List<Message> messages) {
    for (var message : messages) {
      if (Thread.currentThread().isInterrupted()) break;
      log.info("Thread id: " + Thread.currentThread().getName() + ". Message: " + message.getMessage());
    }
    messages.clear();
  }
}