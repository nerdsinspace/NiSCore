package space.nerdsin.plugins.enhancedchat.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import space.nerdsin.plugins.enhancedchat.config.PluginConfiguration;
import space.nerdsin.plugins.enhancedchat.event.PlayerConnectedEvent;
import space.nerdsin.plugins.enhancedchat.event.PlayerHandleExpiredEvent;
import space.nerdsin.plugins.enhancedchat.model.PlayerEntry;
import space.nerdsin.plugins.enhancedchat.services.SchedulerService;
import space.nerdsin.plugins.enhancedchat.services.PlayerService;
import space.nerdsin.plugins.enhancedchat.util.IPluginListener;
import space.nerdsin.plugins.enhancedchat.util.QuickComponents;

@Singleton
public class IgnoreChatListener implements IPluginListener {

  private final PluginConfiguration config;
  private final PlayerService players;
  private final SchedulerService scheduler;
  
  @Inject
  public IgnoreChatListener(PluginConfiguration config, PlayerService players, SchedulerService scheduler) {
    this.config = config;
    this.players = players;
    this.scheduler = scheduler;
  }
  
  @Override
  public boolean isEnabled() {
    return config.isIgnoreListEnabled();
  }
  
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onChatEvent(AsyncPlayerChatEvent event) {
    final PlayerEntry sender = players.getPlayerByUuid(event.getPlayer().getUniqueId());

    if(sender != null) {
      // remove if the recipient is ignoring this player
      event.getRecipients().removeIf(receiver -> sender.isIgnoringMe(receiver.getUniqueId()));
    } else {
      // something is broken in the codes logic or message is sent too early
      // prevent anyone from receiving this message
      event.setCancelled(true);
      event.getPlayer().sendMessage(QuickComponents.error("You are not allowed to send a message right now."));
      config.getLogger().warning(String.format("No player '%s' inside player service cache",
          event.getPlayer().getName()));
    }
  }

  @EventHandler
  public void onPlayerConnect(PlayerConnectedEvent event) {
    // running this async may cause the player to receive a message from
    // a user they have blocked, but this is an acceptable cost.
    scheduler.invokeAsync(event.getPlayer()::loadPlayersIgnoringMe);
  }

  @EventHandler
  public void onPlayerHandleExpire(PlayerHandleExpiredEvent event) {
    // remove this players handle from the ignore lists
    for(PlayerEntry pd : players.getConnectedPlayers()) {
      pd.removePlayerIgnoringMe(event.getPlayer());
    }
  }
}
