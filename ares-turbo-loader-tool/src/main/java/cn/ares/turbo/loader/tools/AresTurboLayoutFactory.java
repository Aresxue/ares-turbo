package cn.ares.turbo.loader.tools;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import org.springframework.boot.loader.tools.CustomLoaderLayout;
import org.springframework.boot.loader.tools.Layout;
import org.springframework.boot.loader.tools.LayoutFactory;
import org.springframework.boot.loader.tools.Layouts;
import org.springframework.boot.loader.tools.LoaderClassesWriter;


public class AresTurboLayoutFactory implements LayoutFactory {

  private static final String NESTED_LOADER_JAR = "META-INF/loader/spring-boot-loader.jar";

  private static final String NESTED_LOADER_JAR_ARES = "META-INF/loader/ares-turbo-loader.jar";

  public static class Jar extends Layouts.Jar implements CustomLoaderLayout {

    @Override
    public void writeLoadedClasses(LoaderClassesWriter writer) throws IOException {
      // 拷贝springboot loader相关的文件到jar根目录
      // Copy springboot Loader-related files to the jar root directory
      writer.writeLoaderClasses(NESTED_LOADER_JAR);
      // 拷贝ares-turbo-loader相关的文件到jar根目录
      // Copy ares-turbo-loader-related files to the jar root directory
      writer.writeLoaderClasses(NESTED_LOADER_JAR_ARES);
    }

    @Override
    public String getLauncherClassName() {
      // 替换为自定义的JarLauncher
      // Replace with a custom JarLauncher
      return "cn.ares.turbo.loader.AresJarLauncher";
    }
  }

  public Layout getLayout(File file) {
    if (file == null) {
      throw new IllegalArgumentException("File must not be null");
    } else {
      String lowerCaseFileName = file.getName().toLowerCase(Locale.ENGLISH);
      if (lowerCaseFileName.endsWith(".jar")) {
        // 返回自定义的jar
        // return custom jar
        return new Jar();
      } else if (lowerCaseFileName.endsWith(".war")) {
        return new Layouts.War();
      } else if (!file.isDirectory() && !lowerCaseFileName.endsWith(".zip")) {
        throw new IllegalStateException("Unable to deduce layout for '" + file + "'");
      } else {
        return new Layouts.Expanded();
      }
    }
  }

}
