package space.nerdsin.plugins.chatcontroller;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatControllerPlugin extends JavaPlugin implements Listener {
 
	private List<String> whitelist = Collections.emptyList();
	private boolean allowOp = true;
	private String blockMessage = "Unknown command. Type /help for help.";
  
  @Override
  public void onEnable() {
    saveDefaultConfig();
    
    this.whitelist =
        Collections.unmodifiableList(getConfig().getStringList("whitelist").stream()
            .map(String::toLowerCase)
            .collect(Collectors.toList()));
    this.allowOp = getConfig().getBoolean("allow-op");
    this.blockMessage = getConfig().getString("block-message");
    
    getServer().getPluginManager().registerEvents(this, this);
  }
  
  private boolean shouldBlock(Player player) {
    return !allowOp || !player.isOp();
  }
  
  private boolean isRestricted(String cmd) {
    return !whitelist.contains(cmd.toLowerCase());
  }
  
  private String getMessageCommand(String message) {
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
      String command = getMessageCommand(event.getMessage()).toLowerCase();
      if(!whitelist.contains(command)) {
        event.getPlayer().sendMessage(blockMessage);
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
      if(shouldBlock(player)) {
        String command = getMessageCommand(event.getBuffer()).toLowerCase();
        if(!whitelist.contains(command)) {
          event.setCancelled(true);
        }
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
      event.getCommands().removeIf(this::isRestricted);
    }
  }
}
