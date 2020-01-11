package space.nerdsin.plugins.enhancedchat.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.event.EventHandler;
import space.nerdsin.plugins.enhancedchat.event.PlayerConnectedEvent;
import space.nerdsin.plugins.enhancedchat.services.PlayerService;
import space.nerdsin.plugins.enhancedchat.services.SchedulerService;
import space.nerdsin.plugins.enhancedchat.services.WhisperService;
import space.nerdsin.plugins.enhancedchat.util.IPluginListener;

@Singleton
public class WhisperChatListener implements IPluginListener {
  private final PlayerService players;
  private final SchedulerService scheduler;
  private final WhisperService whispering;

  @Inject
  public WhisperChatListener(PlayerService players, SchedulerService scheduler, WhisperService whispering) {
    this.players = players;
    this.scheduler = scheduler;
    this.whispering = whispering;
  }

  @EventHandler
  public void onPlayerConnect(PlayerConnectedEvent event) {
    scheduler.invokeAsync(event.getPlayer()::loadLastWhispers);
  }
}
