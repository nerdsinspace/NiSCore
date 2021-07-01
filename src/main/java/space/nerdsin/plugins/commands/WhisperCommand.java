package space.nerdsin.plugins.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import space.nerdsin.plugins.api.IPluginCommand;
import space.nerdsin.plugins.api.QuickComponents;
import space.nerdsin.plugins.config.PluginConfiguration;
import space.nerdsin.plugins.model.PlayerEntry;
import space.nerdsin.plugins.services.PlayerService;
import space.nerdsin.plugins.services.SchedulerService;

import java.util.Arrays;

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
    if (args.length < 2) {
      sender.sendMessage(QuickComponents.forCommandUsage(command.getUsage()));
    } else {
      PlayerEntry self = players.getPlayerByUuid(sender.getUniqueId());
      if (self != null) {
        String targetUsername = args[0];
        if (!self.getUsername().equalsIgnoreCase(targetUsername)) {
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
    var msg = (" " + message.trim());

    // sender should always receive this message
    sender.getPlayer().sendMessage(Component.text()
        .color(NamedTextColor.DARK_PURPLE)
        .content("to ")
        .append(QuickComponents.forUsername(receiver.getUsername()))
        .append(Component.text(":"))
        .append(Component.text(msg, NamedTextColor.GRAY)));

    // update the senders last whisper to
    scheduler.invokeAsync(() -> sender.setLastPlayerWhisperTo(receiver));

    // if the target is ignored by the sender then this operation will not complete
    // however we will try and mask so players cannot learn who is ignoring them
    if (!sender.isIgnoringMe(receiver)) {
      receiver.getPlayer().sendMessage(Component.text()
          .color(NamedTextColor.DARK_PURPLE)
          .append(QuickComponents.forUsername(sender.getUsername()))
          .append(Component.text(" says:"))
          .append(Component.text(msg, NamedTextColor.GRAY)));

      // update the receiver whisper from
      scheduler.invokeAsync(() -> receiver.setLastPlayerWhisperFrom(sender));
    }
  }
}
