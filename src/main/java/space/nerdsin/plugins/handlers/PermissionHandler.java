package space.nerdsin.plugins.handlers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.event.EventHandler;
import space.nerdsin.plugins.config.PluginConfiguration;
import space.nerdsin.plugins.event.PlayerConnectedEvent;
import space.nerdsin.plugins.event.PlayerDisconnectedEvent;
import space.nerdsin.plugins.model.PlayerEntry;
import space.nerdsin.plugins.api.IPluginListener;

@Singleton
public class PermissionHandler implements IPluginListener {
  private final PluginConfiguration config;

  @Inject
  public PermissionHandler(PluginConfiguration config) {
    this.config = config;
  }

  @Override
  public boolean isEnabled() {
    return config.isPermissionsEnabled();
  }

  @EventHandler
  public void onPlayerConnect(PlayerConnectedEvent event) {
    PlayerEntry ply = event.getPlayer();

    // attach to the player
    ply.setPermissionAttachment(ply.getPlayer().addAttachment(config.getPlugin()));

    for(String perm : config.getPermissions()) {
      ply.getPermissionAttachment().setPermission(perm, true);
    }

    // send updated list of commands
    ply.getPlayer().updateCommands();
  }

  @EventHandler
  public void onPlayerDisconnect(PlayerDisconnectedEvent event) {
    PlayerEntry ply = event.getPlayer();

    if(ply.getPermissionAttachment() != null) {
      // detach from the player
      ply.getPlayer().removeAttachment(ply.getPermissionAttachment());
    }
  }
}
