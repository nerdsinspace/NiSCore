package space.nerdsin.plugins;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import space.nerdsin.plugins.services.PluginInitializeService;
import space.nerdsin.plugins.api.IPluginCommand;
import space.nerdsin.plugins.api.IPluginListener;

public class PluginCore extends JavaPlugin implements Listener {
  
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
      throw new Error("Error loading NiSCore, see stacktrace above for details", t);
    }
  }
}
