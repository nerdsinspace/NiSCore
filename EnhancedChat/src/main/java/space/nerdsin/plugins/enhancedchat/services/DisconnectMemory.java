package space.nerdsin.plugins.enhancedchat.services;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;
import org.bukkit.entity.Player;
import space.nerdsin.plugins.enhancedchat.model.PlayerEntry;

public class DisconnectMemory {
	private final Cache<String, PlayerEntry> memory;
  
  public DisconnectMemory(int seconds) {
    memory = CacheBuilder.newBuilder()
        .expireAfterWrite(seconds, TimeUnit.SECONDS)
        .build();
  }
  
  public void addPlayer(Player player) {
    memory.put(player.getName().toLowerCase(), new PlayerEntry(player));
  }
  
  public void removePlayer(Player player) {
    memory.invalidate(player.getName().toLowerCase());
  }
  
  public PlayerEntry getPlayerUUID(String username) {
    return memory.getIfPresent(username.toLowerCase());
  }
}
