package space.nerdsin.plugins.enhancedchat.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import space.nerdsin.plugins.enhancedchat.config.PluginConfiguration;
import space.nerdsin.plugins.enhancedchat.model.PlayerUUID;
import space.nerdsin.plugins.enhancedchat.services.SchedulerService;
import space.nerdsin.plugins.enhancedchat.services.IgnoringService;
import space.nerdsin.plugins.enhancedchat.util.IPluginCommand;
import space.nerdsin.plugins.enhancedchat.util.QuickComponents;
import space.nerdsin.plugins.enhancedchat.util.TextBuilder;

@Singleton
public class IgnoreListCommand implements IPluginCommand {
  
  private final PluginConfiguration config;
  private final IgnoringService ignoring;
  private final SchedulerService scheduler;
  
  @Inject
  public IgnoreListCommand(PluginConfiguration config,
      IgnoringService ignoring, SchedulerService scheduler) {
    this.config = config;
    this.ignoring = ignoring;
    this.scheduler = scheduler;
  }
  
  @Override
  public void onExecute(Player sender, Command command, String label, String[] args) {
    int page = 0;
    
    if(args.length > 1) {
      // provided a second argument, which will be interpreted as the page number
      String arg = args[0];
      try {
        page = Math.max(0, Integer.parseInt(arg));
      } catch (NumberFormatException e) {
        // provided page number was not actually a number
        sender.sendMessage(TextBuilder.builder()
            .color(ChatColor.RED)
            .italic(true)
            .text(arg)
            .italic(false)
            .text(" is not a number.")
            .done());
        return;
      }
    }
    
    final int pageIndex = page;
    final int pageCount = config.getIgnoreListPageSize();
    
    scheduler.invokeAsync(() -> ignoring.getPlayerIgnoreList(sender.getUniqueId(), pageIndex, pageCount))
        .thenAccept(ignoring -> {
          if(!ignoring.isEmpty()) {
            int total = ignoring.size();
            int totalPages = (int) Math.ceil((float) total / (float) pageCount);

            sender.sendMessage(TextBuilder.builder()
                .color(ChatColor.GOLD)
                .text(String.format("Ignoring [%d/%d]", pageIndex + 1, totalPages))
                .done());
            
            for(PlayerUUID pl : ignoring) {
              sender.sendMessage(TextBuilder.builder()
                  .color(ChatColor.WHITE)
                  .text("> ")
                  .color(ChatColor.GREEN)
                  .usernameText(pl.getUsername())
                  .done());
            }
            
            if(pageIndex + 1 > totalPages) {
              sender.sendMessage(TextBuilder.builder()
                  .color(ChatColor.WHITE)
                  .text("> ")
                  .color(ChatColor.GRAY)
                  .text(String.format("...and %d more", total - ((pageIndex + 1) * pageCount)))
                  .done());
            }
          } else {
            // senders ignore list is empty
            sender.sendMessage(QuickComponents.inform("Not ignoring anyone."));
          }
        });
  }
  
  @Override
  public String getCommand() {
    return "ignorelist";
  }
  
  @Override
  public boolean isEnabled() {
    return config.isIgnoreListEnabled();
  }
}
