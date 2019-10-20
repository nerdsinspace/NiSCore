package space.nerdsin.plugins.spawnradius;

import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnRadiusPlugin extends JavaPlugin implements Listener {
  
  private int radius = 0;
  private int spawnX = 0;
  private int spawnZ = 0;
  private boolean normal = false;
  
  @Override
  public void onEnable() {
    getConfig().addDefault("spawn.radius", 500);
    getConfig().addDefault("spawn.pos.x", 0);
    getConfig().addDefault("spawn.pos.z", 0);
    getConfig().addDefault("spawn.pos.normal", false);
    
    this.radius = Math.abs(getConfig().getInt("spawn.radius"));
    this.spawnX = getConfig().getInt("spawn.pos.x");
    this.spawnZ = getConfig().getInt("spawn.pos.z");
    this.normal = getConfig().getBoolean("spawn.pos.normal");
    
    getServer().getPluginManager().registerEvents(this, this);
  }
  
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerRespawn(final PlayerRespawnEvent event) {
    if(!event.isBedSpawn()) {
      Location location = event.getRespawnLocation();
      if(normal) {
        event.setRespawnLocation(randomLocation(location.getBlockX(), location.getBlockZ(), location.getWorld()));
      } else {
        event.setRespawnLocation(randomLocation(spawnX, spawnZ, location.getWorld()));
      }
    }
  }
  
  private Location randomLocation(int originX, int originZ, final World world) {
    int x = originX + random(-radius, radius);
    int z = originZ + random(-radius, radius);
    return new Location(world, x, world.getHighestBlockYAt(x, z), z);
  }
  
  private static int random(int min, int max) {
    return ThreadLocalRandom.current().nextInt(min, max + 1);
  }
}
