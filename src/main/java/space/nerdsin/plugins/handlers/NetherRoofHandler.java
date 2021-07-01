package space.nerdsin.plugins.handlers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spigotmc.event.entity.EntityMountEvent;
import space.nerdsin.plugins.config.NetherControlConfiguration;
import space.nerdsin.plugins.api.IPluginListener;

import java.util.logging.Logger;

@Singleton
public class NetherRoofHandler implements IPluginListener {
  private final NetherControlConfiguration config;
  private final Logger logger;

  @Inject
  public NetherRoofHandler(NetherControlConfiguration config, @Named("plugin.logger") Logger logger) {
    this.config = config;
    this.logger = logger;
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    var player = event.getPlayer();
    if (shouldBlock(player)
        && player.getEyeLocation().getBlockY() >= config.getFlagY()) {

      if (player.isInsideVehicle()) {
        // dismount entity if player is riding one
        player.leaveVehicle();
      }

      switch (config.getMode()) {
        case SETBACK:
          // get the direction vector for movement
          var to = event.getTo().toVector();
          var from = event.getFrom().toVector();
          var dir = from.subtract(to).normalize();

          if (dir.getY() < 0.95D
              || Math.abs(dir.getX()) > 0.25D
              || Math.abs(dir.getZ()) > 0.25D) {
            // set the current position to the former position
            // that way players can still control the pitch and yaw
            var prev = event.getFrom();
            event.getTo().set(prev.getX(), prev.getY(), prev.getZ());
          }
          break;
        case HURT:
          player.damage(config.getDamage());
          break;
        case KILL:
          player.setHealth(0.D);
          break;
      }
    }
  }

  @EventHandler
  public void onPlayerTeleport(PlayerTeleportEvent event) {
    var player = event.getPlayer();
    if (PlayerTeleportEvent.TeleportCause.ENDER_PEARL.equals(event.getCause())
        && shouldBlock(player)
        && event.getTo().getBlockY() >= config.getFlagY()) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onMountEntity(EntityMountEvent event) {
    if (event.getEntity() instanceof Player) {
      Player player = (Player) event.getEntity();
      var mount = event.getMount();
      if (shouldBlock(player) && mount.getBoundingBox().getMaxY() >= config.getFlagY()) {
        event.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    var player = event.getPlayer();
    if (shouldBlock(player)
        && player.getEyeLocation().getBlockY() >= config.getFlagY()) {
      var loc = player.getLocation();
      var world = player.getWorld();

      int startY = Math.min(122, config.getFlagY() - 1);

      if (startY - 3 <= 0) {
        logger.warning("Not enough space to teleport player down");
        return;
      }

      // replace top and bottom blocks with air if they are not already passable or empty
      var top = world.getBlockAt(loc.getBlockX(), startY, loc.getBlockZ());
      var bot = world.getBlockAt(top.getX(), top.getY() - 1, top.getZ());

      if (!top.isEmpty() || !top.isPassable()) {
        top.setType(Material.AIR);
      }

      if (!bot.isEmpty() || !bot.isPassable()) {
        bot.setType(Material.AIR);
      }

      // replace floor with nether rock
      var floor = world.getBlockAt(bot.getX(), bot.getY() - 1, bot.getZ());

      if (floor.isEmpty() || floor.isPassable()) {
        floor.setType(Material.NETHERRACK);
      }

      // teleport player to new location
      var tp = new Location(world, bot.getX() + 0.5D, bot.getY(), bot.getZ() + 0.5D);
      player.teleport(tp);
    }
  }

  private boolean shouldBlock(Player player) {
    return World.Environment.NETHER.equals(player.getWorld().getEnvironment())
        && (config.isIncludeOps() || !player.isOp())
        && (!config.isSurvivalOnly() || GameMode.SURVIVAL.equals(player.getGameMode()));
  }
}
