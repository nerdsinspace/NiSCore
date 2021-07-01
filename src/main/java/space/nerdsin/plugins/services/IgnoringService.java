package space.nerdsin.plugins.services;

import static org.jooq.impl.DSL.*;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jooq.Condition;
import org.jooq.DSLContext;
import space.nerdsin.plugins.codegen.Tables;
import space.nerdsin.plugins.model.PlayerUUID;

@Singleton
public class IgnoringService extends DatabaseAccessBase {
  
  @Inject
  public IgnoringService(DSLContext dsl) {
    super(dsl);
  }
  
  /**
   * Adds a player to the ignore list provided the target isn't already ignored.
   * This method assumes both players are already added into the database.
   * @param ctx database context
   * @param playerUuid UUID of the ignorer
   * @param targetUuid UUID of the ignoree
   * @return false if the ignoree is already ignored
   */
  private static boolean addIgnore(DSLContext ctx, UUID playerUuid, UUID targetUuid) {
    // add the target to the ignore list
    // if the player is already ignored this operation will return 0
    boolean success = ctx.insertInto(Tables.IGNORED_PLAYER)
        .columns(Tables.IGNORED_PLAYER.SUBJECT_ID, Tables.IGNORED_PLAYER.TARGET_ID)
        .values(
            field(select(Tables.PLAYER.ID)
                .from(Tables.PLAYER)
                .where(checkUUID(playerUuid))
                .limit(1)),
            field(select(Tables.PLAYER.ID)
                .from(Tables.PLAYER)
                .where(checkUUID(targetUuid))
                .limit(1)))
        .onDuplicateKeyIgnore()
        .execute() > 0;
  
    if(success) {
      // if the insert succeeds then increment the ignore count for the subject player
      ctx.update(Tables.PLAYER)
          .set(Tables.PLAYER.IGNORE_COUNT, Tables.PLAYER.IGNORE_COUNT.add(1))
          .where(checkUUID(playerUuid))
          .execute();
    }
  
    return success;
  }
  
  public boolean addIgnore(final UUID playerUuid, final UUID targetUuid) {
    return dsl.transactionResult(c -> addIgnore(using(c), playerUuid, targetUuid));
  }
  
  private static boolean deleteIgnore(DSLContext ctx, UUID playerUuid, Condition condition) {
    boolean success = ctx.deleteFrom(Tables.IGNORED_PLAYER)
        .where(Tables.IGNORED_PLAYER.SUBJECT_ID.eq(select(Tables.PLAYER.ID)
            .from(Tables.PLAYER)
            .where(checkUUID(playerUuid))
            .limit(1)))
        .and(Tables.IGNORED_PLAYER.TARGET_ID.eq(select(Tables.PLAYER.ID)
            .from(Tables.PLAYER)
            .where(condition)
            .limit(1)))
        .limit(1)
        .execute() > 0;
  
    if(success) {
      // if the delete succeeds then decrement the ignore count of the subject player
      ctx.update(Tables.PLAYER)
          .set(Tables.PLAYER.IGNORE_COUNT, Tables.PLAYER.IGNORE_COUNT.subtract(1))
          .where(checkUUID(playerUuid))
          .execute();
    }
  
    return success;
  }
  
  private boolean deleteIgnore(final UUID playerUuid, final Condition condition) {
    return dsl.transactionResult(c -> deleteIgnore(using(c), playerUuid, condition));
  }
  
  /**
   * Deletes a player from another players ignore list.
   * @param uuid Ignorer player UUID
   * @param targetUsername Username of the ignoree
   * @return false if no such player is ignored
   */
  public boolean deleteIgnore(UUID uuid, String targetUsername) {
    return deleteIgnore(uuid, Tables.PLAYER.USERNAME.equalIgnoreCase(targetUsername));
  }
  
  /**
   * Deletes a player from another players ignore list.
   * @param uuid Ignorer player uuid
   * @param targetUuid UUID of the ignoree
   * @return false if no such player is ignored
   */
  public boolean deleteIgnore(UUID uuid, UUID targetUuid) {
    return deleteIgnore(uuid, checkUUID(targetUuid));
  }
  
  /**
   * Gets a list of all the players that are ignoring a certain player
   * @param ctx database context
   * @param targetUuid player that is being ignored
   * @return list of players ignoring targetUuid
   */
  private static List<PlayerUUID> getPlayersIgnoring(DSLContext ctx, UUID targetUuid) {
    return ctx.select(Tables.PLAYER.UUID, Tables.PLAYER.USERNAME)
        .from(Tables.IGNORED_PLAYER)
        .innerJoin(Tables.PLAYER).on(Tables.PLAYER.ID.eq(Tables.IGNORED_PLAYER.SUBJECT_ID))
        .where(field(select(Tables.PLAYER.ID)
            .from(Tables.PLAYER)
            .where(checkUUID(targetUuid)))
            .eq(Tables.IGNORED_PLAYER.TARGET_ID))
        .fetch(rec -> new PlayerUUID(rec.getValue(Tables.PLAYER.USERNAME), uuidFromRecord(rec)));
  }
  
  public List<PlayerUUID> getPlayersIgnoring(UUID uuid) {
    return getPlayersIgnoring(dsl, uuid);
  }
  
  /**
   * Gets a limited list of every player that the provided player is ignoring
   * @param ctx database context
   * @param subjectUuid Player to get ignore list for
   * @param limit max number of entries to get
   * @return capped list of players subjectPlayer is ignoring
   */
  private static List<PlayerUUID> getPlayerIgnoreList(DSLContext ctx, UUID subjectUuid, int limit) {
    return ctx.select(Tables.PLAYER.UUID, Tables.PLAYER.USERNAME)
        .from(Tables.IGNORED_PLAYER)
        .innerJoin(Tables.PLAYER).on(Tables.PLAYER.ID.eq(Tables.IGNORED_PLAYER.TARGET_ID))
        .where(field(select(Tables.PLAYER.ID)
            .from(Tables.PLAYER)
            .where(checkUUID(subjectUuid)))
            .eq(Tables.IGNORED_PLAYER.SUBJECT_ID))
        .limit(limit)
        .fetch(rec -> new PlayerUUID(rec.getValue(Tables.PLAYER.USERNAME), uuidFromRecord(rec)));
  }
  
  public List<PlayerUUID> getPlayerIgnoreList(UUID uuid, int pageIndex, int pageLimit) {
    List<PlayerUUID> entries = getPlayerIgnoreList(dsl, uuid, (pageIndex + 1) * pageLimit);
    int startIndex = pageIndex * pageLimit;
    return entries.subList(startIndex, Math.min(entries.size(), startIndex + pageLimit));
  }
  
  /**
   * Gets the number of players a player is ignoring
   * @param ctx database context
   * @param subjectUuid player to lookup
   * @return count of ignored players
   */
  private static Optional<Integer> getPlayerIgnoreListCount(DSLContext ctx, UUID subjectUuid) {
    return ctx.select(Tables.PLAYER.IGNORE_COUNT)
        .from(Tables.PLAYER)
        .where(checkUUID(subjectUuid))
        .limit(1)
        .fetchOptional(Tables.PLAYER.IGNORE_COUNT);
  }
  
  public Optional<Integer> getPlayerIgnoreListCount(UUID uuid) {
    return getPlayerIgnoreListCount(dsl, uuid);
  }
  
  /**
   * Update to every players ignore count. Will correct any issues if they arise.
   * @return CompletionStage instance
   */
  int ignoreCountIntegrityCheck() {
    return dsl.update(Tables.PLAYER)
        .set(Tables.PLAYER.IGNORE_COUNT, selectCount()
            .from(Tables.IGNORED_PLAYER)
            .where(Tables.IGNORED_PLAYER.SUBJECT_ID.eq(Tables.PLAYER.ID)))
        .execute();
  }
}
