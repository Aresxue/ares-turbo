package cn.ares.turbo.loader;

import cn.ares.turbo.loader.fast.FastURLClassLoader;
import java.net.URL;
import org.springframework.boot.loader.JarLauncher;


public class AresJarLauncher extends JarLauncher {

  @Override
  protected ClassLoader createClassLoader(URL[] urls) throws Exception {
    return new FastURLClassLoader(urls, getClass().getClassLoader());
  }

  public static void main(String[] args) throws Exception {
    new AresJarLauncher().launch(args);
  }

}
