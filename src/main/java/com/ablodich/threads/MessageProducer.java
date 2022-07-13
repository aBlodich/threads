package com.ablodich.threads;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageProducer implements Runnable {
  private static final int MESSAGES_COUNT = 10000;
  private final BlockingQueue<Message> queue;
  private final CyclicBarrier barrier;

  public MessageProducer(BlockingQueue<Message> queue, CyclicBarrier barrier) {
    this.queue = queue;
    this.barrier = barrier;
  }

  @Override
  public void run() {
    try {
      while (!Thread.currentThread().isInterrupted()) {
        produce();
      }
    } catch (BrokenBarrierException e) {
      log.info("broken barrier");
    } catch (InterruptedException e) {
      log.info("interrupted");
    }
  }

  private void produce() throws BrokenBarrierException, InterruptedException {
    barrier.await();
    queue.put(new Message(UUID.randomUUID().toString(), 100L));
  }
}