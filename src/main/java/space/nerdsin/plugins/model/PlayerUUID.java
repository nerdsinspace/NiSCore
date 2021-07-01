package space.nerdsin.plugins.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.entity.Player;

@Getter
@AllArgsConstructor
@ToString
public class PlayerUUID {
	private final String username;
	private final UUID uuid;
	
  public PlayerUUID(Player player) {
    this(player.getName(), player.getUniqueId());
  }
  
  @Override
  public int hashCode() {
    return getUuid().hashCode();
  }
  
  @Override
  public boolean equals(Object obj) {
    return obj == this
        || (obj instanceof PlayerUUID && getUuid().equals(((PlayerUUID) obj).getUuid()));
  }
}
