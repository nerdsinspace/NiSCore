package space.nerdsin.plugins.model;

import com.google.common.collect.Sets;

import java.util.*;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import space.nerdsin.plugins.api.IServiceProvider;

@Getter
@Setter
public class PlayerEntry extends PlayerUUID {
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private final IServiceProvider services;

  // List of players currently online ignoring this player
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
	private final Set<PlayerUUID> ignoringMe = Sets.newHashSet();

  private Player player;

	private PlayerUUID lastPlayerWhisperTo = null;
	private PlayerUUID lastPlayerWhisperFrom = null;

	private PermissionAttachment permissionAttachment;

  private boolean disconnected = false;
  
  public PlayerEntry(IServiceProvider services, String username, UUID uuid) {
    super(username, uuid);
    this.services = services;
  }

  public PlayerEntry(IServiceProvider services, Player player) {
    this(services, player.getName(), player.getUniqueId());
  }

  public <T extends PlayerUUID> boolean addPlayerIgnoringMe(T pl) {
    if(services.getIgnoringService().addIgnore(pl.getUuid(), getUuid())) {
      synchronized (ignoringMe) {
        return ignoringMe.add(pl);
      }
    }
    return false;
  }

  public boolean removePlayerIgnoringMe(PlayerUUID pl) {
    if(services.getIgnoringService().deleteIgnore(pl.getUuid(), getUuid())) {
      return playerStoppedIgnoringMe(pl);
    }
    return false;
  }

  public boolean playerStoppedIgnoringMe(PlayerUUID pl) {
    synchronized (ignoringMe) {
      return ignoringMe.remove(pl);
    }
  }

  public boolean loadPlayersIgnoringMe() {
    List<PlayerUUID> players = services.getIgnoringService().getPlayersIgnoring(getUuid()).stream()
        .map(PlayerUUID::getUuid)
        .map(services.getPlayerService()::getPlayerByUuid)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    synchronized (ignoringMe) {
      ignoringMe.clear();
      return ignoringMe.addAll(players);
    }
  }

  public boolean isIgnoringMe(PlayerUUID playerUuid) {
    synchronized (ignoringMe) {
      return ignoringMe.contains(playerUuid);
    }
  }

  public boolean isIgnoringMe(UUID player) {
    return isIgnoringMe(new PlayerUUID("", player));
  }

  public boolean stopIgnoring(String username) {
    return services.getIgnoringService().deleteIgnore(getUuid(), username);
  }

  public void setLastPlayerWhisperTo(PlayerUUID receiver) {
    if(services.getWhisperService().setLastWhisperTo(getUuid(), receiver.getUuid())) {
      this.lastPlayerWhisperTo = receiver;
    }
  }

  public void setLastPlayerWhisperFrom(PlayerUUID sender) {
    if(services.getWhisperService().setLastWhisperFrom(sender.getUuid(), getUuid())) {
      this.lastPlayerWhisperFrom = sender;
    }
  }

  public void loadLastWhispers() {
    lastPlayerWhisperTo = services.getWhisperService().getLastWhisperTo(getUuid()).orElse(null);
    lastPlayerWhisperFrom = services.getWhisperService().getLastWhisperFrom(getUuid()).orElse(null);
  }
}
