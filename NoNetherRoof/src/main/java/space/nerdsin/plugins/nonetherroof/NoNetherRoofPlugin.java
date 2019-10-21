package space.nerdsin.plugins.nonetherroof;

import java.util.Objects;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityMountEvent;

public class NoNetherRoofPlugin extends JavaPlugin implements Listener {
  
  enum Mode {
    SETBACK,
    HURT,
    KILL,
    ;
  }
  
  private int flagY = 128;
  private Mode mode = Mode.SETBACK;
  private double damage = 4.D;
  private boolean survivalOnly = true;
  private boolean includeOps = false;
  
  private boolean shouldBlock(Player player) {
    return Environment.NETHER.equals(player.getWorld().getEnvironment())
        && (includeOps || !player.isOp())
        && (!survivalOnly || GameMode.SURVIVAL.equals(player.getGameMode()));
  }
  
  @Override
  public void onEnable() {
    saveDefaultConfig();
  
    flagY = getConfig().getInt("max-height");
    damage = getConfig().getDouble("damage");
    survivalOnly = getConfig().getBoolean("survival-only");
    includeOps = getConfig().getBoolean("include-ops");
    
    try {
      String str = Objects.requireNonNull(getConfig().getString("mode"));
      mode = Mode.valueOf(str.toUpperCase());
    } catch (NullPointerException | IllegalArgumentException e) {
      getLogger().warning("Unknown mode provided, defaulting to SETBACK");
      mode = Mode.SETBACK;
    }
    
    getServer().getPluginManager().registerEvents(this, this);
  }
  
  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    Player player = event.getPlayer();
    if(shouldBlock(player)
        && player.getEyeLocation().getBlockY() >= flagY) {
      
      if(player.isInsideVehicle()) {
        // dismount entity if player is riding one
        player.leaveVehicle();
      }
      
      switch (mode) {
        case SETBACK:
          // get the direction vector for movement
          Vector to = event.getTo().toVector();
          Vector from = event.getFrom().toVector();
          Vector dir = from.subtract(to).normalize();
          
          if(dir.getY() < 0.95D
              || Math.abs(dir.getX()) > 0.25D
              || Math.abs(dir.getZ()) > 0.25D) {
            // set the current position to the former position
            // that way players can still control the pitch and yaw
            Location prev = event.getFrom();
            event.getTo().set(prev.getX(), prev.getY(), prev.getZ());
          }
          break;
        case HURT:
          player.damage(damage);
          break;
        case KILL:
          player.setHealth(0.D);
          break;
      }
    }
  }
  
  @EventHandler
  public void onPlayerTeleport(PlayerTeleportEvent event) {
    Player player = event.getPlayer();
    if(TeleportCause.ENDER_PEARL.equals(event.getCause())
        && shouldBlock(player)
        && event.getTo().getBlockY() >= flagY) {
      event.setCancelled(true);
    }
  }
  
  @EventHandler
  public void onMountEntity(EntityMountEvent event) {
    if(event.getEntity() instanceof Player) {
      Player player = (Player) event.getEntity();
      Entity mount = event.getMount();
      if(shouldBlock(player)
          && mount.getBoundingBox().getMaxY() >= flagY) {
        event.setCancelled(true);
      }
    }
  }
  
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    if(shouldBlock(player)
        && player.getEyeLocation().getBlockY() >= flagY) {
      Location loc = player.getLocation();
      World world = player.getWorld();
      
      int startY = Math.min(122, flagY - 1);
      
      if(startY - 3 <= 0) {
        getLogger().warning("Not enough space to teleport player down");
        return;
      }
      
      // replace top and bottom blocks with air if they are not already passable or empty
      Block top = world.getBlockAt(loc.getBlockX(), startY, loc.getBlockZ());
      Block bot = world.getBlockAt(top.getX(), top.getY() - 1, top.getZ());
      
      if(!top.isEmpty() || !top.isPassable()) {
        top.setType(Material.AIR);
      }
      
      if(!bot.isEmpty() || !bot.isPassable()) {
        bot.setType(Material.AIR);
      }
      
      // replace floor with nether rock
      Block floor = world.getBlockAt(bot.getX(), bot.getY() - 1, bot.getZ());
      
      if(floor.isEmpty() || floor.isPassable()) {
        floor.setType(Material.NETHERRACK);
      }
      
      // teleport player to new location
      Location tp = new Location(world, bot.getX() + 0.5D, bot.getY(), bot.getZ() + 0.5D);
      player.teleport(tp);
    }
  }
}
