package space.nerdsin.plugins.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.Getter;
import space.nerdsin.plugins.PluginCore;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
@Singleton
public class ChatControlConfiguration {
  private List<Pattern> whitelistedPermissions = Collections.emptyList();
  private boolean allowOp = true;
  private String blockMessage = "Unknown command. Type \"/help\" for help.";

  @Inject
  public ChatControlConfiguration(PluginCore plugin, @Named("plugin.logger") Logger logger) {
    this.whitelistedPermissions = plugin.getConfig().getStringList("chat-control.whitelisted-permissions").stream()
        .map(s -> Arrays.stream(s.split("\\*"))
            .map(Pattern::quote)
            .collect(Collectors.joining(".*")))
        .map(Pattern::compile)
        .collect(Collectors.toUnmodifiableList());
    this.allowOp = plugin.getConfig().getBoolean("chat-control.allow-op");
    this.blockMessage = plugin.getConfig().getString("chat-control.block-message");
  }
}
