package space.nerdsin.plugins.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import space.nerdsin.plugins.PluginCore;

import javax.inject.Named;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.logging.Logger;

@Singleton
public class SchedulerService {
  private final PluginCore plugin;
  private final Executor executor;
  private final Logger logger;
  
  @Inject
  public SchedulerService(PluginCore plugin, Executor executor, @Named("plugin.logger") Logger logger) {
    this.plugin = plugin;
    this.executor = executor;
    this.logger = logger;
  }
  
  public CompletionStage<Void> invokeAsync(Runnable task) {
    return CompletableFuture.runAsync(task, executor);
  }
  
  public <T> CompletionStage<T> invokeAsync(Supplier<T> task) {
    return CompletableFuture.supplyAsync(task, executor);
  }

  public void scheduleSendMessage(final UUID playerUuid, final BaseComponent component) {
    Bukkit.getScheduler().runTask(plugin, () -> {
      Player player = Bukkit.getPlayer(playerUuid);
      if(player != null) {
        player.sendMessage(component);
      } else {
        logger.warning(String.format("Failed to send message \"%s\" to player \"%s\" (probably disconnected)",
            component.toPlainText(), playerUuid.toString()));
      }
    });
  }
}
