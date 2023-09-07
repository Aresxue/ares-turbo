package cn.ares.turbo.loader.fast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Loader Index
 */
class FastLoaderIndex {

  private final Map<String, List<FastLoader>> indexMap = new HashMap<>();
  private final List<FastLoader> fastFileLoaders = new ArrayList<>();
  private final Set<String> indexKeys;

  FastLoaderIndex(final List<FastLoader> loaders, boolean multiThreads) {
    final AtomicInteger index = new AtomicInteger(0);
    final int length = loaders.size();
    final Set<String>[] indexKeysArr = new Set[length];
    // multi-thread
    final int thread = multiThreads ? length > 100 ? 4 : Math.min(length / 10, 4) : 1;
    final CountDownLatch countDownLatch = new CountDownLatch(thread > 0 ? thread : 1);
    final CopyOnWriteArrayList<Throwable> errors = new CopyOnWriteArrayList<Throwable>();
    final Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          int i;
          while ((i = index.getAndIncrement()) < length) {
            indexKeysArr[i] = loaders.get(i).getIndexKeys();
          }
        } catch (Throwable e) {
          errors.add(e);
        } finally {
          countDownLatch.countDown();
        }
      }
    };
    if (thread > 1) {
      for (int i = 0; i < thread; i++) {
        new Thread(runnable).start();
      }
      try {
        countDownLatch.await();
      } catch (Throwable e) {
        errors.add(e);
      }
    } else {
      runnable.run();
    }
    if (!errors.isEmpty()) {
      IllegalStateException exception = new IllegalStateException(
          "FastURLClassLoader build index failed!");
      try {
        for (Throwable e : errors) {
          exception.addSuppressed(e);
        }
      } catch (NoSuchMethodError ignore) {
        // Running on Java 6. Continue.
        exception = new IllegalStateException("FastURLClassLoader build index failed!",
            errors.get(0));
      }
      throw exception;
    }
    for (int i = 0; i < length; i++) {
      final FastLoader loader = loaders.get(i);
      for (String key : indexKeysArr[i]) {
        List<FastLoader> indexLoaders = indexMap.get(key);
        if (indexLoaders == null) {
          indexLoaders = new LinkedList<FastLoader>();
          indexMap.put(key, indexLoaders);
        }
        indexLoaders.add(loader);
      }
      if (loader instanceof FastFileLoader) {
        fastFileLoaders.add(loader);
      }
    }
    indexKeys = Collections.unmodifiableSet(indexMap.keySet());
  }

  List<FastLoader> get(String name) {
    if (name != null && name.startsWith(".")) {
      return fastFileLoaders;
    }
    List<FastLoader> loaders;
    if ((loaders = indexMap.get(name)) == null) {
      /* try the package name again */
      int pos;
      if ((pos = name.lastIndexOf("/")) != -1) {
        loaders = indexMap.get(name.substring(0, pos));
      }
    }
    // if not found, try search in directory (protection mechanisms)
    return loaders != null ? loaders : fastFileLoaders;
  }

  Set<String> getIndexKeys() {
    return indexKeys;
  }
}
