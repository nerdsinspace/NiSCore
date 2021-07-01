package space.nerdsin.plugins.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import space.nerdsin.plugins.config.DatabaseConfiguration;

@Singleton
public class PluginInitializeService {
	
  private final DatabaseConfiguration databaseConfiguration;
  private final ClassLoader pluginClassLoader;
  private final IgnoringService ignoring;
  private final SchedulerService scheduler;
  
  
  @Inject
  public PluginInitializeService(DatabaseConfiguration databaseConfiguration,
      @Named("plugin.classloader") ClassLoader pluginClassLoader,
      IgnoringService ignoring, SchedulerService scheduler) {
    this.databaseConfiguration = databaseConfiguration;
    this.pluginClassLoader = pluginClassLoader;
    this.ignoring = ignoring;
    this.scheduler = scheduler;
  }
  
  public void initialize() {
    databaseConfiguration.initialize(pluginClassLoader);
    scheduler.invokeAsync(ignoring::ignoreCountIntegrityCheck);
  }
}
