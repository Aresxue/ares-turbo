package cn.ares.turbo.loader.fast;

import static cn.ares.turbo.loader.fast.FastFileLoader.FILE;

import cn.ares.turbo.loader.util.CollectionUtil;
import cn.ares.turbo.loader.util.MapUtil;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import sun.net.util.URLUtil;

/**
 * This class is used to maintain a search path of URLs for loading classes and resources from both
 * JAR files and directories.
 */
public class FastURLClassPath {

  static final String USER_AGENT_JAVA_VERSION = "UA-Java-Version";
  static final String JAVA_VERSION;
  static final boolean DISABLE_CP_URL_CHECK;

  static {
    JAVA_VERSION = System.getProperty("java.version");
    String property = System.getProperty("jdk.net.URLClassPath.disableClassPathURLCheck", "true");
    DISABLE_CP_URL_CHECK = property != null && ("true".equals(property) || property.isEmpty());
  }

  /* The original search path of URLs. */
  volatile URL[] path;

  /* The resulting search path of Loaders */
  volatile ArrayList<FastLoader> loaders;

  /* The index of each loader */
  volatile FastLoaderIndex loaderIndex;

  /* Map of each URL opened to its corresponding Loader */
  /* Avoid duplication */
  private final HashMap<String, FastLoader> fastLoaderMap;

  /* Whether this URLClassLoader has been closed yet */
  private volatile boolean closed = false;

  private final Lock lock = new ReentrantLock();

  public FastURLClassPath(URL[] urls) {
    int urlLength = urls.length;
    path = new URL[urlLength];
    System.arraycopy(urls, 0, path, 0, urlLength);
    fastLoaderMap = MapUtil.newHashMap(urlLength);
    loaders = createLoaders(urls, fastLoaderMap);
    loaderIndex = new FastLoaderIndex(loaders, true);
  }

  public void addURL(URL url) {
    URL[] newPath = new URL[path.length + 1];
    System.arraycopy(path, 0, newPath, 0, path.length);
    newPath[path.length] = url;

    ArrayList<FastLoader> newLoaders = new ArrayList<>(loaders);
    newLoaders.addAll(createLoaders(new URL[]{url}, fastLoaderMap));
    FastLoaderIndex newLoaderIndex = new FastLoaderIndex(newLoaders, false);

    this.path = newPath;
    this.loaders = newLoaders;
    this.loaderIndex = newLoaderIndex;
  }

  public List<IOException> closeLoaders() {
    lock.lock();
    try {
      if (closed) {
        return Collections.emptyList();
      }
      List<IOException> result = new ArrayList<>();
      for (FastLoader loader : loaders) {
        try {
          loader.close();
        } catch (IOException e) {
          result.add(e);
        }
      }
      closed = true;
      return result;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Returns the original search path of URLs.
   */
  public URL[] getURLs() {
    final URL[] src = path;
    URL[] copy = new URL[src.length];
    System.arraycopy(src, 0, copy, 0, src.length);
    return copy;
  }

  public Set<String> getIndexKeys() {
    return loaderIndex.getIndexKeys();
  }

  public URL findResource(String name, boolean check) {
    for (FastLoader loader : loaderIndex.get(name)) {
      URL url = loader.findResource(name, check);
      if (url != null) {
        return url;
      }
    }
    return null;
  }

  public FastResource getResource(String name, boolean check) {
    for (FastLoader loader : loaderIndex.get(name)) {
      FastResource fastResource = loader.getResource(name, check);
      if (fastResource != null) {
        return fastResource;
      }
    }
    return null;
  }

  /**
   * Finds all resources on the URL search path with the given name. Returns an enumeration of the
   * URL objects.
   *
   * @param name the resource name
   * @return an Enumeration of all the urls having the specified name
   */
  public Enumeration<URL> findResources(final String name, final boolean check) {
    return new Enumeration<URL>() {
      private int index = 0;
      private final List<FastLoader> loaderList = loaderIndex.get(name);
      private URL url = null;

      private boolean next() {
        if (url != null) {
          return true;
        } else {
          FastLoader loader;
          while (index < loaderList.size()) {
            loader = loaderList.get(index++);
            url = loader.findResource(name, check);
            if (url != null) {
              return true;
            }
          }
          return false;
        }
      }

      public boolean hasMoreElements() {
        return next();
      }

      public URL nextElement() {
        if (!next()) {
          throw new NoSuchElementException();
        }
        URL u = url;
        url = null;
        return u;
      }
    };
  }

  public Enumeration<FastResource> getResources(final String name, final boolean check) {
    return new Enumeration<FastResource>() {
      private int index = 0;
      private final List<FastLoader> loaderList = loaderIndex.get(name);
      private FastResource resource = null;

      private boolean next() {
        if (resource != null) {
          return true;
        } else {
          FastLoader loader;
          while (index < loaderList.size()) {
            loader = loaderList.get(index++);
            resource = loader.getResource(name, check);
            if (resource != null) {
              return true;
            }
          }
          return false;
        }
      }

      public boolean hasMoreElements() {
        return next();
      }

      public FastResource nextElement() {
        if (!next()) {
          throw new NoSuchElementException();
        }
        FastResource fastResource = resource;
        resource = null;
        return fastResource;
      }
    };
  }

  private static ArrayList<FastLoader> createLoaders(URL[] us, HashMap<String, FastLoader> fastLoaderMap) {
    ArrayList<FastLoader> loaders;
    Deque<URL> urls;
    if (null == us) {
      urls = new ArrayDeque<URL>();
      loaders = new ArrayList<FastLoader>();
    } else {
      urls = new ArrayDeque<URL>(us.length);
      loaders = CollectionUtil.newArrayList(us.length);
    }
    pushUrls(urls, us);
    while (!urls.isEmpty()) {
      URL url = urls.pop();
      // Skip this URL if it already has a Loader
      String urlNoFragString = URLUtil.urlNoFragString(url);
      if (fastLoaderMap.containsKey(urlNoFragString)) {
        continue;
      }
      try {
        FastLoader loader = getLoader(url);
        loaders.add(loader);
        fastLoaderMap.put(urlNoFragString, loader);
        pushUrls(urls, loader.getClassPath());
      } catch (IOException e) {
        // Silently ignore for now...
      }
    }
    return loaders;
  }

  private static void pushUrls(Deque<URL> urls, URL[] us) {
    if (us != null) {
      for (int i = us.length - 1; i >= 0; --i) {
        urls.push(us[i]);
      }
    }
  }

  /*
   * Returns the Loader for the specified base URL.
   */
  private static FastLoader getLoader(final URL url) throws IOException {
    String file = url.getFile();
    if ("jar".equals(url.getProtocol())) {
      return new FastJarLoader(url);
    } else if (file != null && file.endsWith("/")) {
      if (FILE.equals(url.getProtocol())) {
        return new FastFileLoader(url);
      } else {
        throw new IllegalStateException("Url protocol not support! URL: " + url);
      }
    } else {
      return new FastJarLoader(url);
    }
  }
}