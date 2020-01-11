package space.nerdsin.plugins.enhancedchat;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.java.JavaPlugin;
import space.nerdsin.plugins.enhancedchat.config.DatabaseConfiguration;
import space.nerdsin.plugins.enhancedchat.services.PluginInitializeService;
import space.nerdsin.plugins.enhancedchat.util.IPluginCommand;
import space.nerdsin.plugins.enhancedchat.util.IPluginListener;

import java.util.Arrays;
import java.util.Map;

public class EnhancedChatPlugin extends JavaPlugin implements Listener {
  
  @Override
  public void onEnable() {
    try {
      Injector injector = Guice.createInjector(new PluginModule(this, getClassLoader()));
  
      // force the database to initialize
      PluginInitializeService init = injector.getInstance(PluginInitializeService.class);
      init.initialize();
      
      injector.getBindings().forEach((key, binding) -> {
        Class<?> type = key.getTypeLiteral().getRawType();
        if(IPluginCommand.class.isAssignableFrom(type)) {
          // register command executors
          
          IPluginCommand obj = (IPluginCommand) injector.getInstance(type);
          PluginCommand cmd = getCommand(obj.getCommand());
          
          if(cmd != null && obj.isEnabled()) {
            cmd.setExecutor(obj);
            cmd.setTabCompleter(obj);
          }
        } else if(IPluginListener.class.isAssignableFrom(type)) {
          // register event listeners
          
          IPluginListener obj = (IPluginListener) injector.getInstance(type);
          
          if(obj.isEnabled()) {
            getServer().getPluginManager().registerEvents(obj, this);
          }
        }
      });
      
    } catch (Throwable t) {
      t.printStackTrace();
      
      // bukkit throws useless exceptions
      throw new Error("Error loading EnhancedChat, see stacktrace above for details", t);
    }
  }
}
