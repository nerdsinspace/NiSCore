package space.nerdsin.plugins.enhancedchat.listeners;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.event.EventHandler;
import space.nerdsin.plugins.enhancedchat.config.PluginConfiguration;
import space.nerdsin.plugins.enhancedchat.event.PlayerConnectedEvent;
import space.nerdsin.plugins.enhancedchat.event.PlayerDisconnectedEvent;
import space.nerdsin.plugins.enhancedchat.model.PlayerEntry;
import space.nerdsin.plugins.enhancedchat.util.IPluginListener;

@Singleton
public class PermissionListener implements IPluginListener {
  private final PluginConfiguration config;

  @Inject
  public PermissionListener(PluginConfiguration config) {
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
