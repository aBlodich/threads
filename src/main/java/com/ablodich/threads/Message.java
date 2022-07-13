package com.ablodich.threads;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class Message implements Delayed {
  private String message;
  private long expiration;

  public Message(String message, long delayTimeMillis) {
    this.message = message;
    this.expiration = System.currentTimeMillis() + delayTimeMillis;
  }

  @Override
  public long getDelay(TimeUnit unit) {
    long remainingDelayMillis = expiration - System.currentTimeMillis();
    return unit.convert(remainingDelayMillis, TimeUnit.MILLISECONDS);
  }

  @Override
  public int compareTo(Delayed o) {
    Message other = (Message) o;
    return Long.compare(expiration, other.expiration);
  }
}