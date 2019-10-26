package space.nerdsin.plugins.enhancedchat.services;

import static org.jooq.impl.DSL.*;
import static space.nerdsin.plugins.enhancedchat.codegen.Tables.IGNORED_PLAYER;
import static space.nerdsin.plugins.enhancedchat.codegen.Tables.PLAYER;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.bukkit.entity.Player;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import space.nerdsin.plugins.enhancedchat.model.PlayerEntry;

public class IgnoreList {
	private final DSLContext dsl;
  
  public IgnoreList(DSLContext dsl) {
    this.dsl = dsl;
  }
  
  /**
   * Adds a player to the ignore list provided the target isn't already ignored.
   * @param ctx database context
   * @param playerUsername Username of the ignorer
   * @param playerUuid UUID of the ignorer
   * @param targetUsername Username of the ignoree
   * @param targetUuid UUID of the ignoree
   * @return false if the ignoree is already ignored
   */
  private static boolean addIgnore(DSLContext ctx,
      String playerUsername, UUID playerUuid,
      String targetUsername, UUID targetUuid) {
    // attempt to add both players to the database
    addPlayerIfNotExists(ctx, playerUsername, playerUuid);
    addPlayerIfNotExists(ctx, targetUsername, targetUuid);
  
    // add the target to the ignore list
    // if the player is already ignored this operation will return 0
    boolean success = ctx.insertInto(IGNORED_PLAYER)
        .columns(IGNORED_PLAYER.SUBJECT_ID, IGNORED_PLAYER.TARGET_ID, IGNORED_PLAYER.IGNORE_TIME)
        .values(
            field(select(PLAYER.ID)
                .from(PLAYER)
                .where(checkUUID(playerUuid))
                .limit(1)),
            field(select(PLAYER.ID)
                .from(PLAYER)
                .where(checkUUID(targetUuid))
                .limit(1)),
            value(Instant.now().getEpochSecond()))
        .onDuplicateKeyIgnore()
        .execute() > 0;
  
    if(success) {
      // if the insert succeeds then increment the ignore count for the subject player
      ctx.update(PLAYER)
          .set(PLAYER.IGNORE_COUNT, PLAYER.IGNORE_COUNT.add(1))
          .where(checkUUID(playerUuid))
          .execute();
    }
  
    return success;
  }
  
  private CompletionStage<Boolean> addIgnoreAsync(final String playerUsername, final UUID playerUuid,
      final String targetUsername, final UUID targetUuid) {
    return dsl.transactionResultAsync(
        c -> addIgnore(using(c), playerUsername, playerUuid, targetUsername, targetUuid));
  }
  public CompletionStage<Boolean> addIgnoreAsync(Player player, String targetUsername, UUID targetUuid) {
    return addIgnoreAsync(player.getName(), player.getUniqueId(), targetUsername, targetUuid);
  }
  
  private static boolean deleteIgnore(DSLContext ctx, UUID playerUuid, Condition condition) {
    boolean success = ctx.deleteFrom(IGNORED_PLAYER)
        .where(IGNORED_PLAYER.SUBJECT_ID.eq(select(PLAYER.ID)
            .from(PLAYER)
            .where(checkUUID(playerUuid))
            .limit(1)))
        .and(IGNORED_PLAYER.TARGET_ID.eq(select(PLAYER.ID)
            .from(PLAYER)
            .where(condition)
            .limit(1)))
        .limit(1)
        .execute() > 0;
  
    if(success) {
      // if the delete succeeds then decrement the ignore count of the subject player
      ctx.update(PLAYER)
          .set(PLAYER.IGNORE_COUNT, PLAYER.IGNORE_COUNT.subtract(1))
          .where(checkUUID(playerUuid))
          .execute();
    }
  
    return success;
  }
  
  private CompletionStage<Boolean> deleteIgnoreAsync(final UUID playerUuid, final Condition condition) {
    return dsl.transactionResultAsync(c -> deleteIgnore(using(c), playerUuid, condition));
  }
  
  /**
   * Deletes a player from another players ignore list.
   * @param player Ignorer player instance
   * @param targetUsername Username of the ignoree
   * @return false if no such player is ignored
   */
  public CompletionStage<Boolean> deleteIgnoreAsync(Player player, String targetUsername) {
    return deleteIgnoreAsync(player.getUniqueId(), PLAYER.USERNAME.equalIgnoreCase(targetUsername));
  }
  
  /**
   * Deletes a player from another players ignore list.
   * @param player Ignorer player instance
   * @param targetUuid UUID of the ignoree
   * @return false if no such player is ignored
   */
  public CompletionStage<Boolean> deleteIgnoreAsync(Player player, UUID targetUuid) {
    return deleteIgnoreAsync(player.getUniqueId(), checkUUID(targetUuid));
  }
  
  /**
   * Gets a list of all the players that are ignoring a certain player
   * @param ctx database context
   * @param targetUuid player that is being ignored
   * @return list of players ignoring targetUuid
   */
  private static List<PlayerEntry> getPlayersIgnoring(DSLContext ctx, UUID targetUuid) {
    return ctx.select(PLAYER.UUID_LSB, PLAYER.UUID_MSB, PLAYER.USERNAME)
        .from(IGNORED_PLAYER)
        .innerJoin(PLAYER).on(PLAYER.ID.eq(IGNORED_PLAYER.SUBJECT_ID))
        .where(field(select(PLAYER.ID)
            .from(PLAYER)
            .where(checkUUID(targetUuid)))
            .eq(IGNORED_PLAYER.TARGET_ID))
        .fetch(rec -> new PlayerEntry(rec.getValue(PLAYER.USERNAME), uuidFromRecord(rec)));
  }
  
  public List<PlayerEntry> getPlayersIgnoring(Player player) {
    return getPlayersIgnoring(dsl, player.getUniqueId());
  }
  
  private CompletionStage<List<PlayerEntry>> getPlayersIgnoringAsync(UUID targetUuid) {
    return dsl.transactionResultAsync(c -> getPlayersIgnoring(using(c), targetUuid));
  }
  
  /**
   * Gets a list of all the players that are ignoring a certain player
   * @param targetPlayer Player that is being ignored
   * @return list of players ignoring targetPlayer
   */
  public CompletionStage<List<PlayerEntry>> getPlayersIgnoringAsync(Player targetPlayer) {
    return getPlayersIgnoringAsync(targetPlayer.getUniqueId());
  }
  
  /**
   * Gets a limited list of every player that the provided player is ignoring
   * @param ctx database context
   * @param subjectUuid Player to get ignore list for
   * @param limit max number of entries to get
   * @return capped list of players subjectPlayer is ignoring
   */
  private static List<PlayerEntry> getPlayerIgnoreList(DSLContext ctx, UUID subjectUuid, int limit) {
    return ctx.select(PLAYER.UUID_LSB, PLAYER.UUID_MSB, PLAYER.USERNAME)
        .from(IGNORED_PLAYER)
        .innerJoin(PLAYER).on(PLAYER.ID.eq(IGNORED_PLAYER.TARGET_ID))
        .where(field(select(PLAYER.ID)
            .from(PLAYER)
            .where(checkUUID(subjectUuid)))
            .eq(IGNORED_PLAYER.SUBJECT_ID))
        .limit(limit)
        .fetch(rec -> new PlayerEntry(rec.getValue(PLAYER.USERNAME), uuidFromRecord(rec)));
  }
  
  public List<PlayerEntry> getPlayerIgnoreList(UUID subjectUuid, int limit) {
    return getPlayerIgnoreList(dsl, subjectUuid, limit);
  }
  
  private CompletionStage<List<PlayerEntry>> getPlayerIgnoreListAsync(UUID subjectUuid, int limit) {
    return dsl.transactionResultAsync(c -> getPlayerIgnoreList(using(c), subjectUuid, limit));
  }
  
  public CompletionStage<List<PlayerEntry>> getPlayerIgnoreListAsync(Player subjectPlayer, int index, int limit) {
    return getPlayerIgnoreListAsync(subjectPlayer.getUniqueId(), (index + 1) * limit)
        .thenApply(entries -> entries.subList(index * limit,
            Math.min(entries.size(), (index * limit) + limit)));
  }
  
  /**
   * Gets the number of players a player is ignoring
   * @param ctx database context
   * @param subjectUuid player to lookup
   * @return count of ignored players
   */
  private static int getPlayerIgnoreListCount(DSLContext ctx, UUID subjectUuid) {
    return ctx.select(PLAYER.IGNORE_COUNT)
        .from(PLAYER)
        .where(checkUUID(subjectUuid))
        .limit(1)
        .fetchOptional(PLAYER.IGNORE_COUNT).orElse(0);
  }
  
  public CompletionStage<Integer> getPlayerIgnoreListCountAsync(Player player) {
    return dsl.transactionResultAsync(c -> getPlayerIgnoreListCount(using(c), player.getUniqueId()));
  }
  
  /**
   * Asynchronous update to the given players username.
   * @param playerUuid Player UUID to search for
   * @param username Players new (or current) username to update to
   * @return CompletionStage instance
   */
  public CompletionStage<Integer> updatePlayerAsync(UUID playerUuid, String username) {
    return dsl.update(PLAYER)
        .set(PLAYER.USERNAME, username)
        .where(checkUUID(playerUuid))
        .executeAsync();
  }
  
  /**
   * Asynchronous update to every players ignore count. Will correct any issues if they arise.
   * @return CompletionStage instance
   */
  public CompletionStage<Integer> ignoreCountIntegrityCheckAsync() {
    return dsl.update(PLAYER)
        .set(PLAYER.IGNORE_COUNT, selectCount()
            .from(IGNORED_PLAYER)
            .where(IGNORED_PLAYER.SUBJECT_ID.eq(PLAYER.ID)))
        .executeAsync();
  }
  
  private static int addPlayerIfNotExists(DSLContext ctx, String username, UUID uuid) {
    return ctx.insertInto(PLAYER, PLAYER.UUID_LSB, PLAYER.UUID_MSB, PLAYER.USERNAME)
        .select(select(val(uuid.getLeastSignificantBits()),
                val(uuid.getMostSignificantBits()),
                val(username)
            ).whereNotExists(selectFrom(PLAYER)
                .where(checkUUID(uuid))
                .limit(1)))
        .execute();
  }
  
  private static Condition checkUUID(UUID uuid) {
    return PLAYER.UUID_LSB.eq(uuid.getLeastSignificantBits())
        .and(PLAYER.UUID_MSB.eq(uuid.getMostSignificantBits()));
  }
  
  private static UUID uuidFromRecord(Record record) {
    return new UUID(record.getValue(PLAYER.UUID_MSB), record.getValue(PLAYER.UUID_LSB));
  }
}
