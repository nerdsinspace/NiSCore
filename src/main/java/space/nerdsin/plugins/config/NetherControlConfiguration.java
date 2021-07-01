package space.nerdsin.plugins.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.Getter;
import space.nerdsin.plugins.PluginCore;
import space.nerdsin.plugins.model.PunishMode;

import java.util.Objects;
import java.util.logging.Logger;

@Getter
@Singleton
public class NetherControlConfiguration {
  private int flagY;
  private PunishMode mode;
  private double damage;
  private boolean survivalOnly;
  private boolean includeOps;

  @Inject
  public NetherControlConfiguration(PluginCore plugin, @Named("plugin.logger") Logger logger) {
    flagY = plugin.getConfig().getInt("nether-control.max-height");
    damage = plugin.getConfig().getDouble("nether-control.damage");
    survivalOnly = plugin.getConfig().getBoolean("nether-control.survival-only");
    includeOps = plugin.getConfig().getBoolean("nether-control.include-ops");

    try {
      String str = Objects.requireNonNull(plugin.getConfig().getString("nether-control.mode"));
      mode = PunishMode.valueOf(str.toUpperCase());
    } catch (NullPointerException | IllegalArgumentException e) {
      logger.warning("Unknown mode provided, defaulting to SETBACK");
      mode = PunishMode.SETBACK;
    }
  }
}
