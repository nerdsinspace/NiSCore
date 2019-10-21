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
  
  private int spawnX = 0;
  private int spawnZ = 0;
  private int radius = 0;
  
  @Override
  public void onEnable() {
    saveDefaultConfig();
  
    this.spawnX = getConfig().getInt("spawn.x");
    this.spawnZ = getConfig().getInt("spawn.z");
    this.radius = Math.abs(getConfig().getInt("radius"));
    
    if(getConfig().getBoolean("original")) {
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
    int x = originX + random(-radius, radius);
    int z = originZ + random(-radius, radius);
    return new Location(world, x, world.getHighestBlockYAt(x, z), z);
  }
  
  private static int random(int min, int max) {
    return ThreadLocalRandom.current().nextInt(min, max + 1);
  }
}
