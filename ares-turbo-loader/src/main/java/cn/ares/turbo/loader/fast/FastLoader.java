package cn.ares.turbo.loader.fast;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

abstract class FastLoader {

  private final URL base;

  /*
   * Creates a new Loader for the specified URL.
   */
  FastLoader(URL url) {
    base = url;
  }

  /*
   * Returns the base URL for this Loader.
   */
  URL getBaseURL() {
    return base;
  }

  abstract Set<String> getIndexKeys();

  URL[] getClassPath() throws IOException {
    return null;
  }

  abstract URL findResource(final String name, boolean check);

  abstract FastResource getResource(final String name, boolean check);

  /*
   * close this loader and release all resources
   * method overridden in sub-classes
   */
  public void close() throws IOException {
  }

}
