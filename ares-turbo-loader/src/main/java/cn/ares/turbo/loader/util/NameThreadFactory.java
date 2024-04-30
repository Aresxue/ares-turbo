package cn.ares.turbo.loader.util;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: Ares
 * @time: 2019-09-30 16:41:00
 * @description: 线程命名工厂
 * @description: Named thread factory
 * @version: JDK 1.8
 */
public final class NameThreadFactory {

  private String nameFormat = null;
  private Boolean daemon = null;
  private Integer priority = null;
  private UncaughtExceptionHandler uncaughtExceptionHandler = null;
  private ThreadFactory backingThreadFactory = null;

  public NameThreadFactory setNameFormat(String nameFormat) {
    format(nameFormat, 0);
    this.nameFormat = nameFormat;
    return this;
  }

  public NameThreadFactory setDaemon(boolean daemon) {
    this.daemon = daemon;
    return this;
  }


  public NameThreadFactory setPriority(int priority) {
    checkArgument(priority >= 1, "Thread priority (%s) must be >= %s", priority, 1);
    checkArgument(priority <= 10, "Thread priority (%s) must be <= %s", priority, 10);
    this.priority = priority;
    return this;
  }

  public NameThreadFactory setUncaughtExceptionHandler(
      UncaughtExceptionHandler uncaughtExceptionHandler) {
    this.uncaughtExceptionHandler = (UncaughtExceptionHandler) checkNotNull(
        uncaughtExceptionHandler);
    return this;
  }


  public NameThreadFactory setThreadFactory(ThreadFactory backingThreadFactory) {
    this.backingThreadFactory = checkNotNull(backingThreadFactory);
    return this;
  }

  public ThreadFactory build() {
    return build(this);
  }

  private static ThreadFactory build(NameThreadFactory builder) {
    final String nameFormat = builder.nameFormat;
    final Boolean daemon = builder.daemon;
    final Integer priority = builder.priority;
    final UncaughtExceptionHandler uncaughtExceptionHandler = builder.uncaughtExceptionHandler;
    final ThreadFactory backingThreadFactory =
        builder.backingThreadFactory != null ? builder.backingThreadFactory
            : Executors.defaultThreadFactory();
    final AtomicLong count = nameFormat != null ? new AtomicLong(0L) : null;
    return new ThreadFactory() {
      @Override
      public Thread newThread(Runnable runnable) {
        Thread thread = backingThreadFactory.newThread(runnable);
        if (nameFormat != null) {
          thread.setName(NameThreadFactory.format(nameFormat, count.getAndIncrement()));
        }

        if (daemon != null) {
          thread.setDaemon(daemon);
        }

        if (priority != null) {
          thread.setPriority(priority);
        }

        if (uncaughtExceptionHandler != null) {
          thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        }

        return thread;
      }
    };
  }

  private static String format(String format, Object... args) {
    return String.format(Locale.ROOT, format, args);
  }

  private static void checkArgument(boolean b, String errorMessageTemplate, int p1,
      int p2) {
    if (!b) {
      throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
    }
  }

  private static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    } else {
      return reference;
    }
  }
}
