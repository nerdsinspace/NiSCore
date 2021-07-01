package space.nerdsin.plugins.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.logging.Logger;

public class QuickComponents {
	public static TextComponent forUsername(String username) {
    TextComponent component = new TextComponent(username);
    component.setItalic(true);
    return component;
  }
  
  public static TextComponent forCommandUsage(String usage) {
    TextComponent component = new TextComponent("usage: " + usage);
    component.setColor(ChatColor.GRAY);
    return component;
  }

  public static BaseComponent forPlayerIsOffline(String username) {
	  return TextBuilder.builder()
        .color(ChatColor.RED)
        .text("Player ")
        .usernameText(username)
        .text(" is offline.")
        .done();
  }
  
  public static TextComponent error(String text) {
    TextComponent component = new TextComponent(text);
    component.setColor(ChatColor.RED);
    return component;
  }
  
  public static TextComponent inform(String text) {
    TextComponent component = new TextComponent(text);
    component.setColor(ChatColor.GRAY);
    return component;
  }

  public static TextComponent whisper(String text) {
    TextComponent component = new TextComponent(text);
    component.setColor(ChatColor.DARK_PURPLE);
    return component;
  }

  public static TextComponent unexpectedError(Logger logger, String reason) {
    StackTraceElement[] e = Thread.currentThread().getStackTrace();
    String func = "<unknown>";
    if(e.length > 1) {
      StackTraceElement st = e[1];
      func = st.getClassName() + "::" + st.getMethodName() + "@" + st.getLineNumber();
    }

    logger.warning(String.format("Unexpected error in %s : %s", func, reason));

    return QuickComponents.error("Unexpected error. Try reconnecting if this continues.");
  }
}
