package space.nerdsin.plugins.enhancedchat.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import space.nerdsin.plugins.enhancedchat.model.PlayerEntry;

public class PlayerDisconnectedEvent extends Event {
  private static final HandlerList handlers = new HandlerList();

  private final PlayerEntry player;

  public PlayerDisconnectedEvent(PlayerEntry player) {
    this.player = player;
  }

  public PlayerEntry getPlayer() {
    return player;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}
