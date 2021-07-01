package space.nerdsin.plugins.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.logging.Logger;

public class QuickComponents {
  public static Component forUsername(String username) {
    return Component.text(username).decorate(TextDecoration.ITALIC);
  }

  public static Component forCommandUsage(String usage) {
    return Component.text("usage: " + usage, NamedTextColor.GRAY);
  }

  public static Component forPlayerIsOffline(String username) {
    return Component.text()
        .color(NamedTextColor.RED)
        .append(Component.text("Player "))
        .append(forUsername(username))
        .append(Component.text(" is offline"))
        .build();
  }

  public static Component error(String text) {
    return Component.text(text, NamedTextColor.RED);
  }

  public static Component inform(String text) {
    return Component.text(text, NamedTextColor.GRAY);
  }

  public static Component whisper(String text) {
    return Component.text(text, NamedTextColor.DARK_PURPLE);
  }

  public static Component unexpectedError(Logger logger, String reason) {
    StackTraceElement[] e = Thread.currentThread().getStackTrace();
    String func = "<unknown>";
    if (e.length > 1) {
      StackTraceElement st = e[1];
      func = st.getClassName() + "::" + st.getMethodName() + "@" + st.getLineNumber();
    }

    logger.warning(String.format("Unexpected error in %s : %s", func, reason));

    return QuickComponents.error("Unexpected error. Try reconnecting if this continues.");
  }
}
