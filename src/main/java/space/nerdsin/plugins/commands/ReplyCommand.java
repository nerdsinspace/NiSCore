package space.nerdsin.plugins.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import space.nerdsin.plugins.config.PluginConfiguration;
import space.nerdsin.plugins.model.PlayerEntry;
import space.nerdsin.plugins.model.PlayerUUID;
import space.nerdsin.plugins.services.PlayerService;
import space.nerdsin.plugins.api.IPluginCommand;
import space.nerdsin.plugins.api.QuickComponents;

@Singleton
public class ReplyCommand implements IPluginCommand {
  private final PluginConfiguration config;
  private final PlayerService players;
  private final WhisperCommand whisperCommand;

  @Inject
  public ReplyCommand(PluginConfiguration config, PlayerService players, WhisperCommand whisperCommand) {
    this.config = config;
    this.players = players;
    this.whisperCommand = whisperCommand;
  }

  @Override
  public void onExecute(Player sender, Command command, String label, String[] args) {
    if(args.length < 1) {
      sender.sendMessage(QuickComponents.forCommandUsage(command.getUsage()));
    } else {
      PlayerEntry self = players.getPlayerByUuid(sender.getUniqueId());
      if(self != null) {
        PlayerUUID from = self.getLastPlayerWhisperFrom();
        if(from != null && !from.getUuid().equals(self.getUuid())) {
          // get online player object
          PlayerEntry receiver = players.getPlayerByUuid(from.getUuid());
          if(receiver != null) {
            String message = String.join(" ", args);
            whisperCommand.sendWhisper(self, receiver, message);
          } else {
            // player is offline
            sender.sendMessage(QuickComponents.forPlayerIsOffline(from.getUsername()));
          }
        } else {
          // has not sent a message to anyone ever
          sender.sendMessage(QuickComponents.error("Nobody has whispered to you yet."));
        }
      } else {
        // this shouldn't happen
        sender.sendMessage(QuickComponents.unexpectedError(config.getLogger(), "::getPlayerByUuid is null"));
      }
    }
  }

  @Override
  public String getCommand() {
    return "reply";
  }

  @Override
  public boolean isEnabled() {
    return config.isWhisperEnabled();
  }

  @Override
  public int getArgumentCount() {
    return 0;
  }
}
