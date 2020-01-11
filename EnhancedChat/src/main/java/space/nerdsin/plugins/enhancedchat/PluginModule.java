package space.nerdsin.plugins.enhancedchat;

import com.google.inject.AbstractModule;
import com.google.inject.ProvidedBy;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.jooq.DSLContext;
import org.reflections.Reflections;
import space.nerdsin.plugins.enhancedchat.config.DatabaseConfiguration;

public class PluginModule extends AbstractModule {
  
  private final EnhancedChatPlugin plugin;
  private final ClassLoader pluginClassLoader;
  
  PluginModule(EnhancedChatPlugin plugin, ClassLoader pluginClassLoader) {
    this.plugin = plugin;
    this.pluginClassLoader = pluginClassLoader;
  }
  
  @Override
  protected void configure() {
    install(new PackageModule("space.nerdsin.plugins.enhancedchat.config"));
    install(new PackageModule("space.nerdsin.plugins.enhancedchat.commands"));
    install(new PackageModule("space.nerdsin.plugins.enhancedchat.listeners"));
  }
  
  @Provides
  @Singleton
  Executor executor() {
    ExecutorService es = Executors.newSingleThreadExecutor();
    
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        if(!es.awaitTermination(15L, TimeUnit.SECONDS)) {
          System.err.println("Failed to shutdown async executor within 15 seconds");
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }));
    
    return es;
  }
  
  @Provides
  @Singleton
  EnhancedChatPlugin plugin() {
    return plugin;
  }

  @Provides
  @Named("plugin.logger")
  @Singleton
  Logger logger() {
    return plugin.getLogger();
  }
  
  @Provides
  DSLContext dsl(DatabaseConfiguration configuration) {
    return configuration.dsl();
  }
  
  @Provides
  @Named("plugin.classloader")
  @Singleton
  ClassLoader pluginClassLoader() {
    return pluginClassLoader;
  }
  
  class PackageModule extends AbstractModule {
    private final String packageName;
    
    PackageModule(String packageName) {
      this.packageName = packageName;
    }
    
    @Override
    protected void configure() {
      Reflections pkg = new Reflections(packageName, pluginClassLoader);
      
      pkg.getTypesAnnotatedWith(Singleton.class)
          .forEach(this::bind);
    }
  }
}
