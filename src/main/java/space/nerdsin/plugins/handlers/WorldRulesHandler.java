package space.nerdsin.plugins.handlers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldLoadEvent;
import space.nerdsin.plugins.config.GameRuleConfiguration;
import space.nerdsin.plugins.api.IPluginListener;

@Singleton
public class WorldRulesHandler implements IPluginListener {
  private final GameRuleConfiguration config;

  @Inject
  public WorldRulesHandler(GameRuleConfiguration config) {
    this.config = config;
  }

  @EventHandler
  public void onWorldLoad(WorldLoadEvent event) {
    config.setWorldRules(event.getWorld());
  }
}
