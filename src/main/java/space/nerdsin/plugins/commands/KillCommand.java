package space.nerdsin.plugins.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import space.nerdsin.plugins.config.PluginConfiguration;
import space.nerdsin.plugins.api.IPluginCommand;

@Singleton
public class KillCommand implements IPluginCommand {
  
  private final PluginConfiguration config;
  
  @Inject
  public KillCommand(PluginConfiguration config) {
    this.config = config;
  }
  
  @Override
  public void onExecute(Player sender, Command command, String label, String[] args) {
    // kill the player
    sender.setHealth(0.D);
  }
  
  @Override
  public String getCommand() {
    return "kill";
  }
  
  @Override
  public boolean isEnabled() {
    return config.isKillEnabled();
  }

  @Override
  public int getArgumentCount() {
    return 0;
  }
}
