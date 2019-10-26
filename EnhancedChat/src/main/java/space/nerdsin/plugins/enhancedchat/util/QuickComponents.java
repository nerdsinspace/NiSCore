package space.nerdsin.plugins.enhancedchat.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

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
}
