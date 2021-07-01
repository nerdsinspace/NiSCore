package space.nerdsin.plugins.services;

import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.selectFrom;
import static org.jooq.impl.DSL.val;

import java.util.UUID;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import space.nerdsin.plugins.codegen.Tables;

class DatabaseAccessBase {
  final DSLContext dsl;
  
  DatabaseAccessBase(DSLContext dsl) {
    this.dsl = dsl;
  }
  
  static int addPlayerIfNotExists(DSLContext ctx, String username, UUID uuid) {
    return ctx.insertInto(Tables.PLAYER, Tables.PLAYER.UUID, Tables.PLAYER.USERNAME)
        .select(select(val(uuid), val(username))
            .whereNotExists(selectFrom(Tables.PLAYER)
                .where(checkUUID(uuid))
                .limit(1)))
        .execute();
  }
  
  static Condition checkUUID(UUID uuid) {
    return Tables.PLAYER.UUID.eq(uuid);
  }
  
  static UUID uuidFromRecord(Record record) {
    return record.getValue(Tables.PLAYER.UUID);
  }
}
