package space.nerdsin.plugins.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import space.nerdsin.plugins.PluginCore;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Singleton
public class GameRuleConfiguration {
  private final Map<GameRule, Object> rules = new HashMap<>();

  @Inject
  public GameRuleConfiguration(PluginCore plugin, @Named("plugin.logger") Logger logger) {
    var section = plugin.getConfig().getConfigurationSection("game-rules");
    if (section != null) {
      for (String key : section.getKeys(false)) {
        GameRule rule = GameRule.getByName(key);
        if (rule != null) {
          final String kn = "game-rules." + key;
          Object value;

          // get the proper object for the type
          if (Integer.class.equals(rule.getType())) {
            value = plugin.getConfig().getInt(kn);
          } else if (Boolean.class.equals(rule.getType())) {
            value = plugin.getConfig().getBoolean(kn);
          } else {
            logger.warning("Unknown game rule type '"
                + rule.getType() + "' for "
                + key + "'");
            break;
          }

          rules.put(rule, value);

          // set the game rule for every world
          for (var world : Bukkit.getServer().getWorlds()) {
            world.setGameRule(rule, value);
          }
        } else {
          logger.warning("Unknown game rule '" + key + "'");
        }
      }
    }
  }

  public void setWorldRules(World world) {
    rules.forEach(world::setGameRule);
  }
}
