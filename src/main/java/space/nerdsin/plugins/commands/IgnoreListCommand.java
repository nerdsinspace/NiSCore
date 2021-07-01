package space.nerdsin.plugins.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import space.nerdsin.plugins.api.IPluginCommand;
import space.nerdsin.plugins.api.QuickComponents;
import space.nerdsin.plugins.config.PluginConfiguration;
import space.nerdsin.plugins.model.PlayerUUID;
import space.nerdsin.plugins.services.IgnoringService;
import space.nerdsin.plugins.services.SchedulerService;

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

    if (args.length > 1) {
      // provided a second argument, which will be interpreted as the page number
      String arg = args[0];
      try {
        page = Math.max(0, Integer.parseInt(arg));
      } catch (NumberFormatException e) {
        // provided page number was not actually a number
        sender.sendMessage(Component.text()
            .color(NamedTextColor.RED)
            .append(Component.text(arg).decorate(TextDecoration.ITALIC))
            .append(Component.text(" is not a number.")));
        return;
      }
    }

    final int pageIndex = page;
    final int pageCount = config.getIgnoreListPageSize();

    scheduler.invokeAsync(() -> ignoring.getPlayerIgnoreList(sender.getUniqueId(), pageIndex, pageCount))
        .thenAccept(ignoring -> {
          if (!ignoring.isEmpty()) {
            int total = ignoring.size();
            int totalPages = (int) Math.ceil((float) total / (float) pageCount);

            var comp = Component.text()
                .append(Component.text(
                    String.format("Ignoring [%d/%d]", pageIndex + 1, totalPages),
                    NamedTextColor.GOLD
                ));

            for (PlayerUUID pl : ignoring) {
              comp.append(Component.newline())
                  .append(Component.text("> ", NamedTextColor.WHITE))
                  .append(QuickComponents.forUsername(pl.getUsername()).color(NamedTextColor.GREEN));
            }

            if (pageIndex + 1 > totalPages) {
              comp.append(Component.newline())
                  .append(Component.text("> ", NamedTextColor.WHITE))
                  .append(Component.text(
                      String.format("...and %d more", total - ((pageIndex + 1) * pageCount)),
                      NamedTextColor.GRAY
                  ));
            }

            sender.sendMessage(comp);
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
