package space.nerdsin.plugins.services;

import static org.jooq.impl.DSL.*;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Optional;
import java.util.UUID;
import org.jooq.DSLContext;
import space.nerdsin.plugins.codegen.Tables;
import space.nerdsin.plugins.model.PlayerUUID;

@Singleton
public class WhisperService extends DatabaseAccessBase {
  
  @Inject
  public WhisperService(DSLContext dsl) {
    super(dsl);
  }

  public boolean setLastWhisperTo(UUID sender, UUID receiver) {
    return dsl.update(Tables.PLAYER)
        .set(Tables.PLAYER.PLAYER_LAST_PM_TO, select(Tables.PLAYER.ID)
            .from(Tables.PLAYER)
            .where(checkUUID(receiver))
            .limit(1))
        .where(checkUUID(sender))
        .limit(1)
        .execute() > 0;
  }

  public boolean setLastWhisperFrom(UUID sender, UUID receiver) {
    return dsl.update(Tables.PLAYER)
        .set(Tables.PLAYER.PLAYER_LAST_PM_FROM, select(Tables.PLAYER.ID)
            .from(Tables.PLAYER)
            .where(checkUUID(sender))
            .limit(1))
        .where(checkUUID(receiver))
        .limit(1)
        .execute() > 0;
  }
  
  public Optional<PlayerUUID> getLastWhisperTo(UUID player) {
    return dsl.select(Tables.PLAYER.UUID, Tables.PLAYER.USERNAME)
        .from(Tables.PLAYER)
        .where(Tables.PLAYER.PLAYER_LAST_PM_TO.eq(select(Tables.PLAYER.ID)
            .from(Tables.PLAYER)
            .where(checkUUID(player))
            .limit(1)))
        .limit(1)
        .fetchOptional(rec -> new PlayerUUID(rec.getValue(Tables.PLAYER.USERNAME), uuidFromRecord(rec)));
  }
  
  public Optional<PlayerUUID> getLastWhisperFrom(UUID player) {
    return dsl.select(Tables.PLAYER.UUID, Tables.PLAYER.USERNAME)
        .from(Tables.PLAYER)
        .where(Tables.PLAYER.ID.eq(select(Tables.PLAYER.PLAYER_LAST_PM_FROM)
            .from(Tables.PLAYER)
            .where(checkUUID(player))
            .limit(1)))
        .limit(1)
        .fetchOptional(rec -> new PlayerUUID(rec.getValue(Tables.PLAYER.USERNAME), uuidFromRecord(rec)));
  }
}
