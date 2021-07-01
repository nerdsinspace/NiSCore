package space.nerdsin.plugins.handlers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerRespawnEvent;
import space.nerdsin.plugins.config.SpawnRadiusConfiguration;
import space.nerdsin.plugins.api.IPluginListener;

import java.util.concurrent.ThreadLocalRandom;

@Singleton
public class PlayerRespawnHandler implements IPluginListener {
  private final SpawnRadiusConfiguration config;

  @Inject
  public PlayerRespawnHandler(SpawnRadiusConfiguration config) {
    this.config = config;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerRespawn(final PlayerRespawnEvent event) {
    if(!event.isBedSpawn() && !event.isAnchorSpawn()) {
      var location = event.getRespawnLocation();
      var sl = location.getWorld().getSpawnLocation();
      event.setRespawnLocation(randomLocation(sl.getBlockX(), sl.getBlockZ(), location.getWorld()));
    }
  }

  private Location randomLocation(int originX, int originZ, final World world) {
    Location best = new Location(world,
        originX, world.getHighestBlockYAt(originX, originZ), originZ);

    for(int i = 0; i < config.getAttempts(); ++i) {
      int x = originX + random(-config.getRadius(), config.getRadius());
      int z = originZ + random(-config.getRadius(), config.getRadius());
      int y = world.getHighestBlockYAt(x, z);

      best = new Location(world, x + 0.5D, y, z + 0.5D);

      // if the block is solid, return it now
      // otherwise keep looping until we run out of attempts
      // or find a proper solid spawn
      var block = world.getBlockAt(x, y - 1, z);
      if(block.getType().isSolid()) {
        return best;
      }
    }
    return best;
  }

  private static int random(int min, int max) {
    return ThreadLocalRandom.current().nextInt(min, max + 1);
  }
}
