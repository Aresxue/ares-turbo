package cn.ares.turbo.loader.tools;

import java.io.IOException;
import org.springframework.boot.loader.tools.CustomLoaderLayout;
import org.springframework.boot.loader.tools.DefaultLayoutFactory;
import org.springframework.boot.loader.tools.Layouts;
import org.springframework.boot.loader.tools.LoaderClassesWriter;


public class AresTurboLayoutFactory extends DefaultLayoutFactory {

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

}
