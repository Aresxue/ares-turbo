package cn.ares.turbo.loader.fast;

import static cn.ares.turbo.loader.fast.FastFileLoader.FILE;
import static cn.ares.turbo.loader.fast.FastURLClassPath.JAVA_VERSION;
import static cn.ares.turbo.loader.fast.FastURLClassPath.USER_AGENT_JAVA_VERSION;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessControlException;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import sun.net.www.ParseUtil;

class FastJarLoader extends FastLoader {

  private static final String INDEX_NAME = "META-INF/INDEX.LIST";

  private static final boolean META_INF_FILENAMES = Boolean.getBoolean(
      "sun.misc.JarIndex.metaInfFilenames");

  private final JarFile jar;
  private final URL csu;
  private volatile Set<String> indexKeys;
  private volatile boolean closed = false;

  /*
   * Creates a new JarLoader for the specified URL referring to
   * a JAR file.
   */
  FastJarLoader(URL url) throws IOException {
    super(url.getPath() != null && url.getPath().endsWith("!/") ? url
        : new URL("jar", "", -1, url + "!/", null));
    csu = url;
    jar = getJarFile(csu);
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
    Enumeration<JarEntry> it = jar.entries();
    while (it.hasMoreElements()) {
      JarEntry entry = it.nextElement();
      String fileName = entry.getName();

      // Skip the META-INF directory, the index, and manifest.
      // Any files in META-INF/ will be indexed explicitly
      if ("META-INF/".equals(fileName) ||
          fileName.equals(INDEX_NAME) ||
          fileName.equals(JarFile.MANIFEST_NAME)) {
        continue;
      }

      if (!META_INF_FILENAMES || !fileName.startsWith("META-INF/")) {
        String packageName;
        int pos;
        if ((pos = fileName.lastIndexOf("/")) != -1) {
          packageName = fileName.substring(0, pos);
        } else {
          packageName = fileName;
        }
        ret.add(packageName);
      } else if (!entry.isDirectory()) {
        // Add files under META-INF explicitly so that certain
        // services, like ServiceLoader, etc, can be located
        // with greater accuracy. Directories can be skipped
        // since each file will be added explicitly.
        ret.add(fileName);

      }
    }
    return ret;
  }

  @Override
  public URL[] getClassPath() throws IOException {
    Manifest man = jar.getManifest();
    if (man != null) {
      Attributes attr = man.getMainAttributes();
      if (attr != null) {
        String value = attr.getValue(Attributes.Name.CLASS_PATH);
        if (value != null) {
          return parseClassPath(csu, value);
        }
      }
    }
    return null;
  }

  /*
   * Parses value of the Class-Path manifest attribute and returns
   * an array of URLs relative to the specified base URL.
   */
  private URL[] parseClassPath(URL base, String value)
      throws MalformedURLException {
    StringTokenizer st = new StringTokenizer(value);
    URL[] urls = new URL[st.countTokens()];
    int i = 0;
    while (st.hasMoreTokens()) {
      String path = st.nextToken();
      URL url =
          FastURLClassPath.DISABLE_CP_URL_CHECK ? new URL(base, path) : tryResolve(base, path);
      if (url != null) {
        urls[i] = url;
        i++;
      }
    }
    if (i == 0) {
      urls = null;
    } else if (i != urls.length) {
      // Truncate nulls from end of array
      urls = Arrays.copyOf(urls, i);
    }
    return urls;
  }

  static URL tryResolve(URL base, String input) throws MalformedURLException {
    if (FILE.equalsIgnoreCase(base.getProtocol())) {
      return tryResolveFile(base, input);
    } else {
      return tryResolveNonFile(base, input);
    }
  }

  /**
   * Attempt to return a file URL by resolving input against a base file URL. The input is an
   * absolute or relative file URL that encodes a file path.
   *
   * @return the resolved URL or null if the input is an absolute URL with a scheme other than file
   * (ignoring case)
   * @throws MalformedURLException
   * @apiNote Nonsensical input such as a Windows file path with a drive letter cannot be
   * disambiguated from an absolute URL so will be rejected (by returning null) by this method.
   */
  static URL tryResolveFile(URL base, String input) throws MalformedURLException {
    int index = input.indexOf(':');
    boolean isFile;
    if (index >= 0) {
      String scheme = input.substring(0, index);
      isFile = FILE.equalsIgnoreCase(scheme);
    } else {
      isFile = true;
    }
    return (isFile) ? new URL(base, input) : null;
  }

  /**
   * Attempt to return a URL by resolving input against a base URL. Returns null if the resolved URL
   * is not contained by the base URL.
   *
   * @return the resolved URL or null
   * @throws MalformedURLException
   */
  static URL tryResolveNonFile(URL base, String input) throws MalformedURLException {
    String child = input.replace(File.separatorChar, '/');
    if (isRelative(child)) {
      URL url = new URL(base, child);
      String bp = base.getPath();
      String urlp = url.getPath();
      int pos = bp.lastIndexOf('/');
      if (pos == -1) {
        pos = bp.length() - 1;
      }
      if (urlp.regionMatches(0, bp, 0, pos + 1)
          && urlp.indexOf("..", pos) == -1) {
        return url;
      }
    }
    return null;
  }

  /**
   * Returns true if the given input is a relative URI.
   */
  static boolean isRelative(String child) {
    try {
      return !URI.create(child).isAbsolute();
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  public void close() throws IOException {
    // closing is lock at higher level
    if (!closed) {
      closed = true;
      // in case not already open.
      jar.close();
    }
  }

  private boolean isOptimizable(URL url) {
    return FILE.equals(url.getProtocol());
  }

  private JarFile getJarFile(URL url) throws IOException {
    // Optimize case where url refers to a local jar file
    if (isOptimizable(url)) {
      String path = null;
      String host = url.getHost();
      if (host == null || host.isEmpty() || "localhost".equalsIgnoreCase(host)) {
        path = ParseUtil.decode(url.getFile());
      }
      if (path == null || !new File(path).exists()) {
        throw new FileNotFoundException(path);
      }
      return new JarFile(path);
    }
    URLConnection urlConnection = getBaseURL().openConnection();
    urlConnection.setRequestProperty(USER_AGENT_JAVA_VERSION, JAVA_VERSION);
    return ((JarURLConnection) urlConnection).getJarFile();
  }

  /*
   * Creates the resource and if the check flag is set to true, checks if
   * is it's okay to return the resource.
   */
  FastResource checkResource(final String name, boolean check, final JarEntry entry) {
    final URL url;
    try {
      url = new URL(getBaseURL(), ParseUtil.encodePath(name, false));
    } catch (MalformedURLException e) {
      return null;
    } catch (AccessControlException e) {
      return null;
    }

    return new FastResource() {
      public String getName() {
        return name;
      }

      public URL getURL() {
        return url;
      }

      public URL getCodeSourceURL() {
        return csu;
      }

      public InputStream getInputStream() throws IOException {
        return jar.getInputStream(entry);
      }

      public int getContentLength() {
        return (int) entry.getSize();
      }

      public Manifest getManifest() throws IOException {
        return jar.getManifest();
      }

      public Certificate[] getCertificates() {
        return entry.getCertificates();
      }

      public CodeSigner[] getCodeSigners() {
        return entry.getCodeSigners();
      }

    };
  }

  /*
   * Returns the URL for a resource with the specified name
   */
  URL findResource(final String name, boolean check) {
    FastResource fastResource = getResource(name, check);
    if (fastResource != null) {
      return fastResource.getURL();
    }
    return null;
  }

  /*
   * Returns the JAR Resource for the specified name.
   */
  @Override
  FastResource getResource(final String name, boolean check) {
    final JarEntry entry = jar.getJarEntry(name);
    return entry != null ? checkResource(name, check, entry) : null;
  }

}
