package space.nerdsin.plugins.services;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.*;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import space.nerdsin.plugins.PluginCore;
import space.nerdsin.plugins.config.PluginConfiguration;
import space.nerdsin.plugins.event.PlayerConnectedEvent;
import space.nerdsin.plugins.event.PlayerDisconnectedEvent;
import space.nerdsin.plugins.event.PlayerHandleExpiredEvent;
import space.nerdsin.plugins.model.PlayerEntry;
import space.nerdsin.plugins.api.IServiceProvider;

@Singleton
public class PlayerService implements IServiceProvider {

	private final Cache<String, PlayerEntry> logoutCache;

	private final Set<PlayerEntry> connected = Sets.newHashSet();
	private volatile Set<PlayerEntry> roConnected = ImmutableSet.of();

	private final PluginCore plugin;

	@Getter
	private final IgnoringService ignoringService;
	@Getter
	private final WhisperService whisperService;

	@Inject
  public PlayerService(PluginConfiguration config, PluginCore plugin, IgnoringService ignoringService, WhisperService whisperService) {
    logoutCache = CacheBuilder.newBuilder()
        .expireAfterWrite(config.getCacheForgetAfter(), TimeUnit.SECONDS)
        .removalListener(notification -> {
          if(RemovalCause.EXPIRED.equals(notification.getCause())) {
            onPlayerExpired((PlayerEntry) notification.getValue());
          }
        })
        .build();
    this.plugin = plugin;
    this.ignoringService = ignoringService;
    this.whisperService = whisperService;
  }

  @Override
  public PlayerService getPlayerService() {
    return this;
  }
  
  public void addPlayer(Player player) {
	  // remove the player from the logout cache
	  logoutCache.invalidate(player.getName().toLowerCase());
	  
	  // add the player to the connected cache
    PlayerEntry ply = new PlayerEntry(this, player);
	  synchronized (connected) {
	    if(!connected.add(ply)) {
	      // get the existing player
        ply = getPlayerByUuid(player.getUniqueId());
      } else {
	      updateReadOnlyConnected();
      }

      ply.setDisconnected(false);
	    ply.setPlayer(player);
    }

    // publish new connect event
    Bukkit.getServer().getPluginManager().callEvent(new PlayerConnectedEvent(ply));
  }
  
  public void removePlayer(Player player) {
	  PlayerEntry ply;

	  synchronized (connected) {
      ply = getPlayerByUuid(player.getUniqueId());
      if (ply != null) {
        ply.setDisconnected(true);
        if(connected.remove(ply)) {
          logoutCache.put(player.getName().toLowerCase(), ply);
          updateReadOnlyConnected();
        } else {
          // will prevent the disconnect event from being fired
          ply = null;
        }
      }
    }

    if(ply != null) {
      Bukkit.getServer().getPluginManager().callEvent(new PlayerDisconnectedEvent(ply));
      ply.setPlayer(null);
    }
  }

  private void updateReadOnlyConnected() {
    roConnected = ImmutableSet.copyOf(connected);
  }

  public Set<PlayerEntry> getConnectedPlayers() {
	  return roConnected;
  }

  public Collection<PlayerEntry> getRecentLoggedOutPlayers() {
	  return logoutCache.asMap().values();
  }
  
  public PlayerEntry getPlayerByUuid(final UUID uuid) {
	  synchronized (connected) {
      for (PlayerEntry ply : connected) {
        if (ply.getUuid().equals(uuid)) {
          return ply;
        }
      }
      return null;
    }
  }
  
  public PlayerEntry getPlayerByUsername(String username) {
	  synchronized (connected) {
      for (PlayerEntry pd : connected) {
        if (pd.getUsername().equalsIgnoreCase(username)) {
          return pd;
        }
      }
      return null;
    }
  }
  
  public PlayerEntry getRecentPlayer(final String username) {
	  PlayerEntry ply = getPlayerByUsername(username);
	  return ply == null ? getLogoutPlayer(username.toLowerCase()) : ply;
  }
  
  public PlayerEntry getLogoutPlayer(String username) {
    return logoutCache.getIfPresent(username.toLowerCase());
  }
  
  private void onPlayerExpired(PlayerEntry player) {
    Bukkit.getScheduler().runTask(plugin, () -> {
      synchronized (connected) {
        // ensure that the player hasn't joined again in the brief period between
        // when this task is scheduled and it executes
        if(getPlayerByUuid(player.getUuid()) == null) {
          Bukkit.getServer().getPluginManager().callEvent(new PlayerHandleExpiredEvent(player));
        }
      }
    });
  }
}
