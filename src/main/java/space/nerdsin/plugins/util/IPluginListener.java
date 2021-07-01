package space.nerdsin.plugins.util;

import org.bukkit.event.Listener;

public interface IPluginListener extends Listener {
	default boolean isEnabled() {
	  return true;
  }
}
