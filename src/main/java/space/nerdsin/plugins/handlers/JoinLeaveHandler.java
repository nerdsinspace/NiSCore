package space.nerdsin.plugins.handlers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import space.nerdsin.plugins.services.SchedulerService;
import space.nerdsin.plugins.services.GeneralPlayerService;
import space.nerdsin.plugins.services.PlayerService;
import space.nerdsin.plugins.api.IPluginListener;

@Singleton
public class JoinLeaveHandler implements IPluginListener {
  
  private final PlayerService players;
  private final GeneralPlayerService general;
  private final SchedulerService scheduler;
  
  @Inject
  public JoinLeaveHandler(PlayerService players, GeneralPlayerService general,
      SchedulerService scheduler) {
    this.players = players;
    this.general = general;
    this.scheduler = scheduler;
  }
  
  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerJoin(PlayerJoinEvent event) {
    final String username = event.getPlayer().getName();
    final UUID uuid = event.getPlayer().getUniqueId();
    
    // update the players username
    scheduler.invokeAsync(() -> general.updatePlayerUsername(username, uuid))
        .thenAccept(updated -> {
          if(!updated) {
            // if the player was not updated they must not be in the database
            general.addPlayer(username, uuid);
          }
        });
    
    // add the player to the cache
    players.addPlayer(event.getPlayer());
  }
  
  private void onPlayerLeave(Player player) {
    // remove the player from the cache
    players.removePlayer(player);
  }
  
  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerQuit(PlayerQuitEvent event) {
    onPlayerLeave(event.getPlayer());
  }
  
  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerKicked(PlayerKickEvent event) {
    onPlayerLeave(event.getPlayer());
  }
}
