package cn.ares.turbo.loader.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author: Ares
 * @time: 2022-02-09 16:26:01
 * @description: Lru cache
 * @version: JDK 1.7
 */
public class BytesLruCache extends LinkedHashMap<String, byte[]> {

  private static final long serialVersionUID = -8336395808351083625L;

  private static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;
  private static final float DEFAULT_LOAD_FACTOR = 0.75f;
  private static final ReentrantReadWriteLock READ_WRITE_LOCK = new ReentrantReadWriteLock();
  private static final Lock READ_LOCK = READ_WRITE_LOCK.readLock();
  private static final Lock WRITE_LOCK = READ_WRITE_LOCK.writeLock();

  private final int maxWeight;
  private final AtomicInteger used = new AtomicInteger(0);

  public BytesLruCache(int maxWeight) {
    super(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, true);
    this.maxWeight = maxWeight;
  }

  @Override
  protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
    boolean remove = used.get() > maxWeight;
    if (remove) {
      byte[] cacheBytes = eldest.getValue();
      used.getAndAdd(-cacheBytes.length);
    }
    return remove;
  }

  @Override
  public boolean containsKey(Object key) {
    READ_LOCK.lock();
    try {
      return super.containsKey(key);
    } finally {
      READ_LOCK.unlock();
    }
  }

  @Override
  public byte[] get(Object key) {
    READ_LOCK.lock();
    try {
      return super.get(key);
    } finally {
      READ_LOCK.unlock();
    }
  }

  @Override
  public byte[] put(String key, byte[] bytes) {
    WRITE_LOCK.lock();
    try {
      used.getAndAdd(bytes.length);
      byte[] oldBytes = super.put(key, bytes);
      if (null != oldBytes) {
        used.getAndAdd(-oldBytes.length);
      }
      return oldBytes;
    } finally {
      WRITE_LOCK.unlock();
    }
  }

  @Override
  public byte[] remove(Object key) {
    WRITE_LOCK.lock();
    try {
      return super.remove(key);
    } finally {
      WRITE_LOCK.unlock();
    }
  }

  @Override
  public int size() {
    READ_LOCK.lock();
    try {
      return super.size();
    } finally {
      READ_LOCK.unlock();
    }
  }

  @Override
  public void clear() {
    WRITE_LOCK.lock();
    try {
      super.clear();
    } finally {
      WRITE_LOCK.unlock();
    }
  }

}
