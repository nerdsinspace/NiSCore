package space.nerdsin.plugins.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import space.nerdsin.plugins.api.IPluginCommand;
import space.nerdsin.plugins.api.QuickComponents;
import space.nerdsin.plugins.config.PluginConfiguration;
import space.nerdsin.plugins.model.PlayerEntry;
import space.nerdsin.plugins.services.PlayerService;
import space.nerdsin.plugins.services.SchedulerService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class IgnoreCommand implements IPluginCommand {

  private final PluginConfiguration config;
  private final PlayerService players;
  private final SchedulerService scheduler;

  @Inject
  public IgnoreCommand(PluginConfiguration config,
      PlayerService players,
      SchedulerService scheduler) {
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
      String un = args[0];

      if (un.isEmpty() || un.length() > config.getUsernameMaxLength()) {
        // username is too short or too long
        sender.sendMessage(QuickComponents.error("Invalid username."));
      } else {
        final PlayerEntry self = players.getPlayerByUuid(sender.getUniqueId());

        if (self == null) {
          // something is broken in the code logic
          sender.sendMessage(QuickComponents.unexpectedError(config.getLogger(), "::getPlayerByUuid is null"));
        } else {
          final PlayerEntry target = players.getRecentPlayer(un);

          if (target == null) {
            // the username the sender wants to ignore isn't currently on the server
            // and also not in the player cache, so the operation fails.
            // this is done so that players cannot spam entries into the database
            sender.sendMessage(
                QuickComponents.error("No player by that name is currently on the server."));
          } else if (sender.getUniqueId().equals(target.getUuid())) {
            // cannot ignore yourself
            sender.sendMessage(QuickComponents.error("Cannot ignore yourself."));
          } else {
            scheduler.invokeAsync(() -> target.addPlayerIgnoringMe(self))
                .thenAccept(added -> {
                  if (!added) {
                    // the insert failed, so a matching entry must already exist
                    sender.sendMessage(Component.text()
                        .color(NamedTextColor.RED)
                        .append(QuickComponents.forUsername(target.getUsername()))
                        .append(Component.text(" is already ignored")));
                  } else {
                    // the player has been successfully ignored
                    sender.sendMessage(Component.text()
                        .color(NamedTextColor.GRAY)
                        .append(QuickComponents.forUsername(target.getUsername()))
                        .append(Component.text(" ignored")));
                  }
                });
          }
        }
      }
    }
  }

  @Override
  public List<String> onTabCompletion(Player player, Command command, String alias, String[] args) {
    if (args.length > 1) {
      return Collections.emptyList();
    } else {
      final String arg = args[0].toLowerCase();
      return Stream.concat(Bukkit.getOnlinePlayers().stream().map(Player::getName),
          players.getRecentLoggedOutPlayers().stream().map(PlayerEntry::getUsername))
          .filter(un -> !un.equalsIgnoreCase(player.getName()))
          .filter(un -> un.toLowerCase().startsWith(arg))
          .distinct()
          .sorted(String.CASE_INSENSITIVE_ORDER)
          .limit(10)
          .collect(Collectors.toList());
    }
  }

  @Override
  public String getCommand() {
    return "ignore";
  }

  @Override
  public boolean isEnabled() {
    return config.isIgnoreListEnabled();
  }
}
