package space.nerdsin.plugins.handlers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.permissions.Permissible;
import space.nerdsin.plugins.config.ChatControlConfiguration;
import space.nerdsin.plugins.api.IPluginListener;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class ChatControlHandler implements IPluginListener {
  private final ChatControlConfiguration config;

  @Inject
  public ChatControlHandler(ChatControlConfiguration config) {
    this.config = config;
  }

  private boolean shouldBlock(Player player) {
    return !config.isAllowOp() || !player.isOp();
  }

  private boolean isRestricted(Permissible permissible, Command cmd) {
    if(cmd == null) {
      // this shouldn't happen
      return true;
    }

    if(cmd.getPermission() != null) {
      // ensure player has proper permission and the permission is whitelisted
      return !permissible.hasPermission(cmd.getPermission()) ||
          config.getWhitelistedPermissions().stream()
              .noneMatch(perm -> perm.matcher(cmd.getPermission()).find());
    }

    return true;
  }

  private boolean isRestricted(Permissible permissible, String command) {
    return isRestricted(permissible, Bukkit.getCommandMap().getCommand(command));
  }

  private String getCommandString(String message) {
    if(message.startsWith("/"))
      message = message.substring(1);

    int s = message.indexOf(' ');
    if(s > -1)
      message = message.substring(0, s);

    return message;
  }

  /**
   * Block the server from processing commands from players that shouldn't be able to
   * @param event event
   */
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPreCommandProcess(PlayerCommandPreprocessEvent event) {
    if(shouldBlock(event.getPlayer())) {
      if(isRestricted(event.getPlayer(), getCommandString(event.getMessage()))) {
        event.getPlayer().sendMessage(config.getBlockMessage());
        event.setCancelled(true);
      }
    }
  }

  /**
   * Do not tab complete commands that are blocked
   * @param event event
   */
  @EventHandler(priority = EventPriority.LOWEST)
  public void onTabComplete(TabCompleteEvent event) {
    if(event.getSender() instanceof Player) {
      Player player = (Player) event.getSender();
      if(shouldBlock(player) && isRestricted(player, getCommandString(event.getBuffer()))) {
        event.setCancelled(true);
      }
    }
  }

  /**
   * Do not send the player any commands that are not whitelisted
   * @param event event
   */
  @EventHandler(priority = EventPriority.LOWEST)
  public void onCommandSend(PlayerCommandSendEvent event) {
    if(shouldBlock(event.getPlayer())) {
      // generate a filtered list with restricted commands removed
      // and duplicates also removed
      List<String> commands = event.getCommands().stream()
          .map(Bukkit.getCommandMap()::getCommand)
          .distinct()
          .filter(cmd -> !isRestricted(event.getPlayer(), cmd))
          .map(Command::getName)
          .sorted(String.CASE_INSENSITIVE_ORDER)
          .collect(Collectors.toList());

      event.getCommands().clear();
      event.getCommands().addAll(commands);
    }
  }
}
