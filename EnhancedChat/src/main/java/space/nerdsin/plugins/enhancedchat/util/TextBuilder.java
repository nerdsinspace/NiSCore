package space.nerdsin.plugins.enhancedchat.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class TextBuilder {
  public static TextBuilder builder() {
    return new TextBuilder();
  }

  private final TextComponent root = new TextComponent();

  public TextBuilder color(ChatColor color) {
    root.setColor(color);
    return this;
  }

  public TextBuilder bold(boolean bold) {
    root.setBold(bold);
    return this;
  }

  public TextBuilder italic(boolean italic) {
    root.setItalic(italic);
    return this;
  }

  public TextBuilder underlined(boolean underlined) {
    root.setUnderlined(underlined);
    return this;
  }

  public TextBuilder strikethrough(boolean strikethrough) {
    root.setStrikethrough(strikethrough);
    return this;
  }

  public TextBuilder obfuscated(boolean obfuscated) {
    root.setObfuscated(obfuscated);
    return this;
  }

  public TextBuilder text(String text) {
    TextComponent tc = new TextComponent(text);
    tc.copyFormatting(root);
    root.addExtra(tc);
    return this;
  }

  public TextBuilder usernameText(String username) {
    boolean old = root.isItalic();
    return italic(true).text(username).italic(old);
  }

  public BaseComponent done() {
    return root;
  }
}
