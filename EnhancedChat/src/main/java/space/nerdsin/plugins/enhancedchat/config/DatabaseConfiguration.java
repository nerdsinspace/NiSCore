package space.nerdsin.plugins.enhancedchat.config;

import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.logging.javautil.JavaUtilLogCreator;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.sqlite.SQLiteDataSource;

public class DatabaseConfiguration {
  private final Executor executor = Executors.newSingleThreadExecutor();
  private final Path database;
  
  public DatabaseConfiguration(Path database) {
    this.database = database;
  }
  
  private Executor executor() {
    return executor;
  }
  
  private DataSource dataSource() {
    SQLiteDataSource ds = new SQLiteDataSource();
    ds.setUrl("jdbc:sqlite:" + database.toAbsolutePath().toString());
    return ds;
  }
  
  private DefaultConfiguration configuration() {
    DefaultConfiguration config = new DefaultConfiguration();
    config.set(SQLDialect.SQLITE);
    config.set(new DataSourceConnectionProvider(dataSource()));
    config.setExecutorProvider(this::executor);
    return config;
  }
  
  public DSLContext dsl() {
    return new DefaultDSLContext(configuration());
  }
  
  public void initialize(ClassLoader classLoader) throws FlywayException {
    LogFactory.setLogCreator(new JavaUtilLogCreator());
    
    try {
      Flyway flyway = Flyway.configure(classLoader)
          .locations("classpath:db/migration")
          .dataSource(dataSource())
          .load();
  
      flyway.migrate();
    } catch (Throwable t) {
      // so that you actually get a useful stacktrace
      t.printStackTrace();
      // throw again to stop the plugin from loading
      throw t;
    }
  }
}
