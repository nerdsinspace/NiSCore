package space.nerdsin.plugins.enhancedchat;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jooq.DSLContext;
import space.nerdsin.plugins.enhancedchat.config.DatabaseConfiguration;
import space.nerdsin.plugins.enhancedchat.model.PlayerEntry;
import space.nerdsin.plugins.enhancedchat.services.IgnoreList;
import space.nerdsin.plugins.enhancedchat.services.DisconnectMemory;
import space.nerdsin.plugins.enhancedchat.util.QuickComponents;

public class EnhancedChatPlugin extends JavaPlugin implements Listener {
  private DatabaseConfiguration database;
  
  private DisconnectMemory memory;
  private IgnoreList ignoreList;
  
  private boolean ignoreListEnabled = true;
  private int ignoreListPageSize = 10;
  
  @Override
  public void onEnable() {
    saveDefaultConfig();
    
    ignoreListEnabled = getConfig().getBoolean("ignorelist.enabled");
    ignoreListPageSize = getConfig().getInt("ignorelist.page-size");
    
    // load the database
    String filename = Objects.requireNonNull(getConfig().getString("database.filename"),
        "Missing 'database.filename' option from config");
    database = new DatabaseConfiguration(getDataFolder().toPath().resolve(filename));
    database.initialize(getClassLoader());
    
    DSLContext dsl = database.dsl();
    
    // initialize the services
    memory = new DisconnectMemory(getConfig().getInt("disconnect-memory.forget-after"));
    ignoreList = new IgnoreList(dsl);
    
    // ensure that the ignore count field for each player is accurate
    ignoreList.ignoreCountIntegrityCheckAsync();
    
    getServer().getPluginManager().registerEvents(this, this);
  }
  
  private PlayerEntry lookupPlayer(String username) {
    return Optional.ofNullable(Bukkit.getPlayer(username))
        .filter(Player::isOnline)
        .map(PlayerEntry::new)
        .orElseGet(() -> memory.getPlayerUUID(username));
  }
  
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if(sender instanceof Player) {
      Player player = (Player) sender;
      
      switch (command.getName().toLowerCase()) {
        case "ignore": {
          if (args.length < 1) {
            // not enough arguments provided
            sender.sendMessage(QuickComponents.forCommandUsage(command.getUsage()));
          } else {
            final PlayerEntry pl = lookupPlayer(args[0]);
            
            if (pl == null) {
              // the username the sender wants to ignore isn't currently on the server
              // and also not in the player cache, so the operation fails.
              // this is done so that players cannot spam entries into the database
              sender.sendMessage(
                  QuickComponents.error("No player by that name is currently on the server."));
            } else if (player.getUniqueId().equals(pl.getUuid())) {
              // cannot ignore yourself
              sender.sendMessage(QuickComponents.error("Cannot ignore yourself."));
            } else {
              ignoreList.addIgnoreAsync(player, pl.getUsername(), pl.getUuid())
                  .thenAccept(added -> {
                    if(!added) {
                      // the insert failed, so a matching entry must already exist
                      
                      TextComponent parent = QuickComponents.forUsername(pl.getUsername());
                      parent.setColor(ChatColor.RED);
                      parent.addExtra(QuickComponents.error(" is already ignored."));
      
                      sender.sendMessage(parent);
                    } else {
                      // the player has been successfully ignored
                      TextComponent parent = QuickComponents.forUsername(pl.getUsername());
                      parent.setColor(ChatColor.GRAY);
                      parent.addExtra(QuickComponents.inform(" ignored."));
      
                      sender.sendMessage(parent);
                    }
                  });
            }
          }
          
          return true;
        }
        case "unignore": {
          if (args.length < 1) {
            // not enough arguments provided
            sender.sendMessage(QuickComponents.forCommandUsage(command.getUsage()));
          } else {
            final String username = args[0];
            ignoreList.deleteIgnoreAsync(player, username).thenAccept(removed -> {
              if(!removed) {
                // failed to delete the given username from the list, which means it doesn't
                // exist in the database
  
                TextComponent parent = QuickComponents.error("Not ignoring anyone by the name ");
  
                TextComponent child1 = QuickComponents.forUsername(username);
                child1.setColor(ChatColor.RED);
                parent.addExtra(child1);
                parent.addExtra(QuickComponents.error("."));
  
                sender.sendMessage(parent);
              } else {
                // ignore has been successfully removed from the database
  
                TextComponent parent = QuickComponents.forUsername(username);
                parent.setColor(ChatColor.GRAY);
                parent.addExtra(QuickComponents.inform(" is no longer ignored."));
  
                sender.sendMessage(parent);
              }
            });
          }
          
          return true;
        }
        case "ignorelist": {
          int page = 0;
          
          if(args.length > 1) {
            // provided a second argument, which will be interpreted as the page number
            String arg = args[0];
            try {
              page = Math.max(0, Integer.parseInt(arg));
            } catch (NumberFormatException e) {
              // provided page number was not actually a number
              
              TextComponent parent = QuickComponents.error(arg);
              parent.setItalic(true);
              parent.addExtra(QuickComponents.error(" is not a number."));
              
              sender.sendMessage(parent);
              return true;
            }
          }
          
          final int pageIndex = page;
          ignoreList.getPlayerIgnoreListAsync(player, page, ignoreListPageSize)
              .thenAccept(ignoring -> {
                if(!ignoring.isEmpty()) {
                  int total = ignoring.size();
                  int totalPages = (int) Math.ceil((float) total / (float) ignoreListPageSize);
  
                  TextComponent header = new TextComponent(
                      String.format("Ignoring [%d/%d]", pageIndex + 1, totalPages));
                  header.setColor(ChatColor.GOLD);
                  sender.sendMessage(header);
  
                  for(PlayerEntry pl : ignoring) {
                    TextComponent component = new TextComponent("> ");
                    component.setColor(ChatColor.WHITE);
                    TextComponent child1 = QuickComponents.forUsername(pl.getUsername());
                    child1.setColor(ChatColor.GREEN);
                    component.addExtra(child1);
                    sender.sendMessage(component);
                  }
                  
                  if(pageIndex + 1 >= totalPages) {
                    TextComponent component = new TextComponent("> ");
                    component.setColor(ChatColor.WHITE);
                    TextComponent child1 = new TextComponent(String.format("...and %d more",
                        total - ((pageIndex + 1) * ignoreListPageSize)));
                    child1.setColor(ChatColor.GRAY);
                    component.addExtra(child1);
                  }
                } else {
                  // senders ignore list is empty
                  sender.sendMessage(QuickComponents.inform("Not ignoring anyone."));
                }
              });
          
          return true;
        }
      }
    }
    return super.onCommand(sender, command, label, args);
  }
  
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onChatEvent(AsyncPlayerChatEvent event) {
    event.getRecipients().removeAll(ignoreList.getPlayersIgnoring(event.getPlayer()).stream()
        .map(PlayerEntry::getUuid)
        .map(Bukkit::getPlayer)
        .filter(Objects::nonNull)
        .collect(Collectors.toList()));
  }
  
  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player pl = event.getPlayer();
    memory.removePlayer(pl);
    ignoreList.updatePlayerAsync(pl.getUniqueId(), pl.getName());
  }
  
  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerQuit(PlayerQuitEvent event) {
    memory.addPlayer(event.getPlayer());
  }
  
  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerKicked(PlayerKickEvent event) {
    memory.addPlayer(event.getPlayer());
  }
}
