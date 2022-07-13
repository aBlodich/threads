package com.ablodich.threads;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@AllArgsConstructor
public class MessageSender implements Runnable {
  private volatile List<Message> messages;

  public synchronized void sendMessages(List<Message> messages) {
    for (Message message : messages) {
      if (Thread.currentThread().isInterrupted()) {
        Thread.currentThread().interrupt();
        break;
      }
      log.info("Thread id: " + Thread.currentThread().getName() + ". Message: " + message.getMessage());
    }
  }

  @Override
  public void run() {
      sendMessages(messages);
  }
}
