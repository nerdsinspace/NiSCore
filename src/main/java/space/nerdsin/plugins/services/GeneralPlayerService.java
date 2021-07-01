package space.nerdsin.plugins.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.UUID;
import org.jooq.DSLContext;
import space.nerdsin.plugins.codegen.Tables;

@Singleton
public class GeneralPlayerService extends DatabaseAccessBase {
  
  @Inject
  public GeneralPlayerService(DSLContext dsl) {
    super(dsl);
  }
  
  /**
   * Adds the player to the database if they do not already exist.
   * @param username Players current username
   * @param uuid Players unique ID
   * @return false if the player was not added
   */
  public boolean addPlayer(String username, UUID uuid) {
    return addPlayerIfNotExists(dsl, username, uuid) > 0;
  }
  
  /**
   * Asynchronous update to the given players username.
   * @param username Players new (or current) username to update to
   * @param uuid Player UUID to search for
   * @return CompletionStage chain
   */
  public boolean updatePlayerUsername(String username, UUID uuid) {
    return dsl.update(Tables.PLAYER)
        .set(Tables.PLAYER.USERNAME, username)
        .where(checkUUID(uuid))
        .limit(1)
        .execute() > 0;
  }
}
