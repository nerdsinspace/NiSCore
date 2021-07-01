package space.nerdsin.plugins.handlers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.event.EventHandler;
import space.nerdsin.plugins.event.PlayerConnectedEvent;
import space.nerdsin.plugins.services.PlayerService;
import space.nerdsin.plugins.services.SchedulerService;
import space.nerdsin.plugins.services.WhisperService;
import space.nerdsin.plugins.api.IPluginListener;

@Singleton
public class WhisperChatHandler implements IPluginListener {
  private final PlayerService players;
  private final SchedulerService scheduler;
  private final WhisperService whispering;

  @Inject
  public WhisperChatHandler(PlayerService players, SchedulerService scheduler, WhisperService whispering) {
    this.players = players;
    this.scheduler = scheduler;
    this.whispering = whispering;
  }

  @EventHandler
  public void onPlayerConnect(PlayerConnectedEvent event) {
    scheduler.invokeAsync(event.getPlayer()::loadLastWhispers);
  }
}
