package cn.ares.turbo.loader.fast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import sun.net.www.ParseUtil;

class FastFileLoader extends FastLoader {

  protected static final String FILE = "file";
  /* Canonicalized File */
  private final File dir;
  private volatile Set<String> indexKeys;

  FastFileLoader(URL url) throws IOException {
    super(url);
    if (!FILE.equals(url.getProtocol())) {
      throw new IllegalArgumentException("url");
    }
    String path = url.getFile().replace('/', File.separatorChar);
    path = ParseUtil.decode(path);
    dir = (new File(path)).getCanonicalFile();
  }

  @Override
  Set<String> getIndexKeys() {
    if (indexKeys == null) {
      indexKeys = getIndexKeys0();
    }
    return indexKeys;
  }

  private Set<String> getIndexKeys0() {
    Set<String> ret = new HashSet<>();
    if (dir.isDirectory()) {
      ret.add("");
      for (File subFile : dir.listFiles()) {
        String name = subFile.getName();
        ret.add(name);
        if (subFile.isDirectory()) {
          collectDir(ret, name + "/", subFile);
        }
      }
    }
    return ret;
  }

  private void collectDir(Set<String> paths, String prefix, File dir) {
    for (File subFile : dir.listFiles()) {
      if (subFile.isDirectory()) {
        String path = prefix + subFile.getName();
        paths.add(path);
        collectDir(paths, path + "/", subFile);
      }
    }
  }

  /*
   * Returns the URL for a resource with the specified name
   */
  URL findResource(final String name, boolean check) {
    FastResource rsc = getResource(name, check);
    if (rsc != null) {
      return rsc.getURL();
    }
    return null;
  }

  FastResource getResource(final String name, boolean check) {
    final URL url;
    try {
      URL normalizedBase = new URL(getBaseURL(), ".");
      url = new URL(getBaseURL(), ParseUtil.encodePath(name, false));

      if (!url.getFile().startsWith(normalizedBase.getFile())) {
        // requested resource had ../..'s in path
        return null;
      }

      final File file;
      if (name.contains("..")) {
        file = (new File(dir, name.replace('/', File.separatorChar)))
            .getCanonicalFile();
        if (!((file.getPath()).startsWith(dir.getPath()))) {
          /* outside of base dir */
          return null;
        }
      } else {
        file = new File(dir, name.replace('/', File.separatorChar));
      }

      if (file.exists()) {
        return new FastResource() {
          public String getName() {
            return name;
          }

          public URL getURL() {
            return url;
          }

          public URL getCodeSourceURL() {
            return getBaseURL();
          }

          public InputStream getInputStream() throws IOException {
            return Files.newInputStream(file.toPath());
          }

          public int getContentLength() throws IOException {
            return (int) file.length();
          }
        };
      }
    } catch (Exception e) {
      return null;
    }
    return null;
  }
}
