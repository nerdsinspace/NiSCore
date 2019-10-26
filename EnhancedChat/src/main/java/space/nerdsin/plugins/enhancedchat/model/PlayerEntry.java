package space.nerdsin.plugins.enhancedchat.model;

import java.util.UUID;
import org.bukkit.entity.Player;

public class PlayerEntry {
	private final String username;
	private final UUID uuid;
  
  public PlayerEntry(String username, UUID uuid) {
    this.username = username;
    this.uuid = uuid;
  }
  public PlayerEntry(Player player) {
    this(player.getName(), player.getUniqueId());
  }
  
  public String getUsername() {
    return username;
  }
  
  public UUID getUuid() {
    return uuid;
  }
}
