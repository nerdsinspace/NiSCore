package space.nerdsin.plugins.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import space.nerdsin.plugins.PluginCore;

@Getter
@Singleton
public class SpawnRadiusConfiguration {
  private int spawnX;
  private int spawnZ;
  private int radius;
  private int attempts;
  private boolean original;

  @Inject
  public SpawnRadiusConfiguration(PluginCore plugin) {
    this.spawnX = plugin.getConfig().getInt("spawn-radius.x");
    this.spawnZ = plugin.getConfig().getInt("spawn-radius.z");
    this.radius = plugin.getConfig().getInt("spawn-radius.radius");
    this.attempts = plugin.getConfig().getInt("spawn-radius.attempts");
    this.original = plugin.getConfig().getBoolean("spawn-radius.original");
  }
}
