package space.nerdsin.plugins.api;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public interface IPluginCommand extends CommandExecutor, TabCompleter {
  String getCommand();
	boolean isEnabled();

	default int getArgumentCount() {
	  return 1;
  }

	void onExecute(Player sender, Command command, String label, String[] args);
  default List<String> onTabCompletion(Player player, Command command, String alias, String[] args) {
    return args.length > getArgumentCount() ? Collections.emptyList() :
        getOnlineUsernames(args[0].toLowerCase(), player.getUniqueId());
  }

  @Override
  default boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if(!(sender instanceof Player)) {
      return false;
    }
    onExecute((Player) sender, command, label, args);
    return true;
  }

  @Override
  default List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    if(!(sender instanceof Player)) {
      return Collections.emptyList();
    }
    return onTabCompletion((Player) sender, command, alias, args);
  }

  static List<String> getOnlineUsernames(final String filter, final UUID exclude) {
    return Bukkit.getOnlinePlayers().stream()
        .filter(pl -> !pl.getUniqueId().equals(exclude))
        .map(Player::getName)
        .filter(un -> un.toLowerCase().startsWith(filter))
        .sorted(String.CASE_INSENSITIVE_ORDER)
        .limit(10)
        .collect(Collectors.toList());
  }
}
