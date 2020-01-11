package space.nerdsin.plugins.enhancedchat.util;

import org.bukkit.event.Listener;

public interface IPluginListener extends Listener {
	default boolean isEnabled() {
	  return true;
  }
}
