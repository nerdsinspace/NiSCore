package space.nerdsin.plugins.enhancedchat.services;

import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.selectFrom;
import static org.jooq.impl.DSL.val;
import static space.nerdsin.plugins.enhancedchat.codegen.Tables.PLAYER;

import java.util.UUID;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;

class DatabaseAccessBase {
  final DSLContext dsl;
  
  DatabaseAccessBase(DSLContext dsl) {
    this.dsl = dsl;
  }
  
  static int addPlayerIfNotExists(DSLContext ctx, String username, UUID uuid) {
    return ctx.insertInto(PLAYER, PLAYER.UUID, PLAYER.USERNAME)
        .select(select(val(uuid), val(username))
            .whereNotExists(selectFrom(PLAYER)
                .where(checkUUID(uuid))
                .limit(1)))
        .execute();
  }
  
  static Condition checkUUID(UUID uuid) {
    return PLAYER.UUID.eq(uuid);
  }
  
  static UUID uuidFromRecord(Record record) {
    return record.getValue(PLAYER.UUID);
  }
}
