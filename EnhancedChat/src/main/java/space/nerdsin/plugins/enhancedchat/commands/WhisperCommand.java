package space.nerdsin.plugins.enhancedchat.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.server.TabCompleteEvent;
import space.nerdsin.plugins.enhancedchat.config.PluginConfiguration;
import space.nerdsin.plugins.enhancedchat.model.PlayerEntry;
import space.nerdsin.plugins.enhancedchat.services.PlayerService;
import space.nerdsin.plugins.enhancedchat.services.SchedulerService;
import space.nerdsin.plugins.enhancedchat.util.IPluginCommand;
import space.nerdsin.plugins.enhancedchat.util.QuickComponents;
import space.nerdsin.plugins.enhancedchat.util.TextBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class WhisperCommand implements IPluginCommand {
  private final PluginConfiguration config;
  private final PlayerService players;
  private final SchedulerService scheduler;

  @Inject
  public WhisperCommand(PluginConfiguration config, PlayerService players,
      SchedulerService scheduler) {
    this.config = config;
    this.players = players;
    this.scheduler = scheduler;
  }

  @Override
  public void onExecute(Player sender, Command command, String label, String[] args) {
    if(args.length < 2) {
      sender.sendMessage(QuickComponents.forCommandUsage(command.getUsage()));
    } else {
      PlayerEntry self = players.getPlayerByUuid(sender.getUniqueId());
      if(self != null) {
        String targetUsername = args[0];
        if(!self.getUsername().equalsIgnoreCase(targetUsername)) {
          PlayerEntry receiver = players.getPlayerByUsername(targetUsername);
          if (receiver != null) {
            String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            sendWhisper(self, receiver, message);
          } else {
            // player is offline
            sender.sendMessage(QuickComponents.forPlayerIsOffline(targetUsername));
          }
        } else {
          sender.sendMessage(QuickComponents.error("Cannot whisper to yourself."));
        }
      } else {
        // this shouldn't happen
        sender.sendMessage(QuickComponents.unexpectedError(config.getLogger(), "::getPlayerByUuid is null"));
      }
    }
  }

  @Override
  public String getCommand() {
    return "whisper";
  }

  @Override
  public boolean isEnabled() {
    return config.isWhisperEnabled();
  }

  public void sendWhisper(PlayerEntry sender, PlayerEntry receiver, String message) {
    // sender should always receive this message
    sender.getPlayer().sendMessage(TextBuilder.builder()
        .color(ChatColor.DARK_PURPLE)
        .text("to ")
        .usernameText(receiver.getUsername())
        .text(":")
        .color(ChatColor.GRAY)
        .text(" " + message)
        .done());
    // update the senders last whisper to
    scheduler.invokeAsync(() -> sender.setLastPlayerWhisperTo(receiver));

    // if the target is ignored by the sender then this operation will not complete
    // however we will try and mask so players cannot learn who is ignoring them
    if(!sender.isIgnoringMe(receiver)) {
      receiver.getPlayer().sendMessage(TextBuilder.builder()
          .color(ChatColor.DARK_PURPLE)
          .usernameText(sender.getUsername())
          .text(" says:")
          .color(ChatColor.GRAY)
          .text(" " + message)
          .done());

      // update the receiver whisper from
      scheduler.invokeAsync(() -> receiver.setLastPlayerWhisperFrom(sender));
    }
  }
}
