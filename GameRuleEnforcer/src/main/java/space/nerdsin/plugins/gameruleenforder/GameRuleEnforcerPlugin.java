package space.nerdsin.plugins.gameruleenforder;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class GameRuleEnforcerPlugin extends JavaPlugin {
  
  @Override
  public void onEnable() {
    saveDefaultConfig();
    
    ConfigurationSection section = getConfig().getConfigurationSection("game-rules");
    if(section != null) {
      for(String key : section.getKeys(false)) {
        GameRule rule = GameRule.getByName(key);
        if(rule != null) {
          final String kn = "game-rules." + key;
          Object value;
  
          // get the proper object for the type
          if(Integer.class.equals(rule.getType())) {
            value = getConfig().getInt(kn);
          } else if(Boolean.class.equals(rule.getType())) {
            value = getConfig().getBoolean(kn);
          } else {
            getLogger().warning("Unknown game rule type '"
                + rule.getType() + "' for "
                + key + "'");
            break;
          }
          
          // set the game rule for every world
          for(World world : getServer().getWorlds()) {
            world.setGameRule(rule, value);
          }
        } else {
          getLogger().warning("Unknown game rule '" + key + "'");
        }
      }
    }
  }
}
