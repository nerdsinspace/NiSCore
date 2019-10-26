package space.nerdsin.plugins.chatcontroller;

import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatControllerPlugin extends JavaPlugin implements Listener {
 
	private List<String> whitelist = Collections.emptyList();
	private boolean allowOp = false;
  
  @Override
  public void onEnable() {
    saveDefaultConfig();
    
    this.whitelist = getConfig().getStringList("whitelist");
    
    getServer().getPluginManager().registerEvents(this, this);
  }
  
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    return super.onCommand(sender, command, label, args);
  }
}
