package space.nerdsin.plugins.api;

import org.bukkit.event.Listener;

public interface IPluginListener extends Listener {
	default boolean isEnabled() {
	  return true;
  }
}
