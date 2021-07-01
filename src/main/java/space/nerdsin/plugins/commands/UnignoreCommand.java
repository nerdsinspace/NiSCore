package space.nerdsin.plugins.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import space.nerdsin.plugins.config.PluginConfiguration;
import space.nerdsin.plugins.model.PlayerEntry;
import space.nerdsin.plugins.services.SchedulerService;
import space.nerdsin.plugins.services.PlayerService;
import space.nerdsin.plugins.api.IPluginCommand;
import space.nerdsin.plugins.api.QuickComponents;
import space.nerdsin.plugins.api.TextBuilder;

@Singleton
public class UnignoreCommand implements IPluginCommand {
  
  private final PluginConfiguration config;
  private final PlayerService players;
  private final SchedulerService scheduler;
  
  @Inject
  public UnignoreCommand(PluginConfiguration config,
      PlayerService players, SchedulerService scheduler) {
    this.config = config;
    this.players = players;
    this.scheduler = scheduler;
  }
  
  @Override
  public void onExecute(Player sender, Command command, String label, String[] args) {
    if (args.length < 1) {
      // not enough arguments provided
      sender.sendMessage(QuickComponents.forCommandUsage(command.getUsage()));
    } else {
      final PlayerEntry self = players.getPlayerByUuid(sender.getUniqueId());
      if(self == null) {
        // this shouldn't happen
        sender.sendMessage(QuickComponents.unexpectedError(config.getLogger(), "::getPlayerByUuid is null"));
      } else {
        final String username = args[0];
        scheduler.invokeAsync(() -> self.stopIgnoring(username))
            .thenAccept(removed -> {
              if (!removed) {
                // failed to delete the given username from the list, which means it doesn't
                // exist in the database
                sender.sendMessage(TextBuilder.builder()
                    .color(ChatColor.RED)
                    .text("Not ignoring anyone by the name ")
                    .usernameText(username)
                    .text(".")
                    .done());
              } else {
                // ignore has been successfully removed from the database

                // update any online players in memory database
                PlayerEntry ply = players.getPlayerByUsername(username);
                if(ply != null) {
                  ply.playerStoppedIgnoringMe(self);
                }

                sender.sendMessage(TextBuilder.builder()
                    .color(ChatColor.GRAY)
                    .usernameText(username)
                    .text(" is no longer ignored.")
                    .done());
              }
            });
      }
    }
  }
  
  @Override
  public String getCommand() {
    return "unignore";
  }
  
  @Override
  public boolean isEnabled() {
    return config.isIgnoreListEnabled();
  }
}
