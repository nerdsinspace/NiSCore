package space.nerdsin.plugins.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import com.google.inject.name.Named;
import lombok.Getter;
import org.jooq.SQLDialect;
import space.nerdsin.plugins.PluginCore;

@Getter
@Singleton
public class PluginConfiguration {
	private final PluginCore plugin;
  private final Logger logger;

  private SQLDialect databaseDialect;
  private String databaseMode;
  private String databaseConnection;
  private String databaseExtra;
  private String databaseUsername;
  private String databasePassword;
  private Path databasePath;
  
  private boolean ignoreListEnabled;
  private int ignoreListPageSize;
  
  private boolean killEnabled;

  private boolean whisperEnabled;

  private boolean permissionsEnabled;
  private List<String> permissions;
  
  private int cacheForgetAfter;
  
  @SuppressWarnings("ConstantConditions")
  @Inject
  public PluginConfiguration(PluginCore plugin, @Named("plugin.logger") Logger logger) {
    this.plugin = plugin;
    this.logger = logger;

    // save the default config file if it does not already exist
    plugin.saveDefaultConfig();

    this.databaseDialect = SQLDialect.valueOf(plugin.getConfig().getString("database.dialect", "H2"));
    this.databaseMode = plugin.getConfig().getString("database.mode");
    this.databaseConnection = plugin.getConfig().getString("database.connection");
    this.databaseExtra = plugin.getConfig().getString("database.extra");
    this.databaseUsername = plugin.getConfig().getString("database.username");
    this.databasePassword = plugin.getConfig().getString("database.password");
    
    this.databasePath = plugin.getDataFolder().toPath()
        .resolve(plugin.getConfig().getString("database.filename", "data.sqlite"));
    
    this.ignoreListEnabled = plugin.getConfig().getBoolean("ignorelist.enabled", true);
    this.ignoreListPageSize = plugin.getConfig().getInt("ignorelist.page-size", 10);
    
    this.killEnabled = plugin.getConfig().getBoolean("kill.enabled", true);

    this.whisperEnabled = plugin.getConfig().getBoolean("whisper.enabled", true);

    this.permissionsEnabled = plugin.getConfig().getBoolean("permission.enabled", true);
    this.permissions = plugin.getConfig().getStringList("permissions.defaults");
    
    this.cacheForgetAfter = plugin.getConfig()
        .getInt("disconnect-memory.forget-after", 15);
  }
  
  public int getUsernameMaxLength() {
    return 16;
  }
}
