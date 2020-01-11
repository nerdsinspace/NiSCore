package space.nerdsin.plugins.enhancedchat.services;

import static org.jooq.impl.DSL.*;
import static space.nerdsin.plugins.enhancedchat.codegen.Tables.PLAYER;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Optional;
import java.util.UUID;
import org.jooq.DSLContext;
import space.nerdsin.plugins.enhancedchat.model.PlayerUUID;

@Singleton
public class WhisperService extends DatabaseAccessBase {
  
  @Inject
  public WhisperService(DSLContext dsl) {
    super(dsl);
  }

  public boolean setLastWhisperTo(UUID sender, UUID receiver) {
    return dsl.update(PLAYER)
        .set(PLAYER.PLAYER_LAST_PM_TO, select(PLAYER.ID)
            .from(PLAYER)
            .where(checkUUID(receiver))
            .limit(1))
        .where(checkUUID(sender))
        .limit(1)
        .execute() > 0;
  }

  public boolean setLastWhisperFrom(UUID sender, UUID receiver) {
    return dsl.update(PLAYER)
        .set(PLAYER.PLAYER_LAST_PM_FROM, select(PLAYER.ID)
            .from(PLAYER)
            .where(checkUUID(sender))
            .limit(1))
        .where(checkUUID(receiver))
        .limit(1)
        .execute() > 0;
  }
  
  public Optional<PlayerUUID> getLastWhisperTo(UUID player) {
    return dsl.select(PLAYER.UUID, PLAYER.USERNAME)
        .from(PLAYER)
        .where(PLAYER.PLAYER_LAST_PM_TO.eq(select(PLAYER.ID)
            .from(PLAYER)
            .where(checkUUID(player))
            .limit(1)))
        .limit(1)
        .fetchOptional(rec -> new PlayerUUID(rec.getValue(PLAYER.USERNAME), uuidFromRecord(rec)));
  }
  
  public Optional<PlayerUUID> getLastWhisperFrom(UUID player) {
    return dsl.select(PLAYER.UUID, PLAYER.USERNAME)
        .from(PLAYER)
        .where(PLAYER.ID.eq(select(PLAYER.PLAYER_LAST_PM_FROM)
            .from(PLAYER)
            .where(checkUUID(player))
            .limit(1)))
        .limit(1)
        .fetchOptional(rec -> new PlayerUUID(rec.getValue(PLAYER.USERNAME), uuidFromRecord(rec)));
  }
}
