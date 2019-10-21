package space.nerdsin.plugins.spawnradius;

import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnRadiusPlugin extends JavaPlugin implements Listener {
  
  private int spawnX = 0;
  private int spawnZ = 0;
  private int radius = 500;
  private int attempts = 16;
  
  @Override
  public void onEnable() {
    saveDefaultConfig();
  
    this.spawnX = getConfig().getInt("spawn.x");
    this.spawnZ = getConfig().getInt("spawn.z");
    this.radius = Math.abs(getConfig().getInt("radius"));
    this.attempts = Math.max(1, getConfig().getInt("attempts"));
    
    if(!getConfig().getBoolean("original")) {
      getServer().getWorlds().forEach(world -> world.setSpawnLocation(
          spawnX,
          world.getHighestBlockYAt(spawnX, spawnZ),
          spawnZ
      ));
    }
    
    getServer().getPluginManager().registerEvents(this, this);
  }
  
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerRespawn(final PlayerRespawnEvent event) {
    if(!event.isBedSpawn()) {
      Location location = event.getRespawnLocation();
      Location sl = location.getWorld().getSpawnLocation();
      event.setRespawnLocation(randomLocation(sl.getBlockX(), sl.getBlockZ(), location.getWorld()));
    }
  }
  
  private Location randomLocation(int originX, int originZ, final World world) {
    Location best = new Location(world,
        originX, world.getHighestBlockYAt(originX, originZ), originZ);
    
    for(int i = 0; i < attempts; ++i) {
      int x = originX + random(-radius, radius);
      int z = originZ + random(-radius, radius);
      int y = world.getHighestBlockYAt(x, z);
      
      best = new Location(world, x + 0.5D, y, z + 0.5D);
      
      // if the block is solid, return it now
      // otherwise keep looping until we run out of attempts
      // or find a proper solid spawn
      Block block = world.getBlockAt(x, y - 1, z);
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
