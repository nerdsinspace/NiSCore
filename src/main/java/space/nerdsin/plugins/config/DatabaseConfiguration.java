package space.nerdsin.plugins.config;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.Executor;
import javax.sql.DataSource;
import lombok.AccessLevel;
import lombok.Getter;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.internal.logging.javautil.JavaUtilLogCreator;
import org.h2.jdbcx.JdbcDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;

@Singleton
public class DatabaseConfiguration {
  private final PluginConfiguration config;
  
  @Getter(AccessLevel.PRIVATE)
  private final Executor executor;
  
  @Inject
  public DatabaseConfiguration(PluginConfiguration config, Executor executor) {
    this.config = config;
    this.executor = executor;
  }
  
  private DataSource dataSource() {
    switch (config.getDatabaseDialect()) {
      case H2:
      {
        JdbcDataSource ds = new JdbcDataSource();

        String url = "jdbc:h2:";

        if(!Strings.isNullOrEmpty(config.getDatabaseMode())) {
          url += config.getDatabaseMode() + ":";
        }

        if(!Strings.isNullOrEmpty(config.getDatabaseConnection())) {
          url += config.getDatabaseConnection()
              .replaceAll("\\{FILENAME\\}", config.getDatabasePath().toAbsolutePath().toString());
        }

        if(!Strings.isNullOrEmpty(config.getDatabaseExtra())) {
          url += ";" + config.getDatabaseExtra();
        }

        ds.setUrl(url);

        String user = Strings.emptyToNull(config.getDatabaseUsername());
        if(user != null) {
          // only set if a username is present
          ds.setUser(user);
          ds.setPassword(Strings.nullToEmpty(config.getDatabasePassword()));
        }

        return ds;
      }
      default: {
        config.getLogger().warning("Tried to load data source from unsupported database." +
            " Defaulting to in-memory H2 database (changes will not be saved)");
        JdbcDataSource ds = new JdbcDataSource();
        ds.setUrl("jdbc:h2:mem:temp");
        return ds;
      }
    }
  }
  
  public void initialize(ClassLoader classLoader) {
    // set the flyway logger creator
    // if not set then this will crash
    LogFactory.setLogCreator(new JavaUtilLogCreator());

    String location;
    switch (config.getDatabaseDialect()) {
      case H2:
      default:
        location = "classpath:db/h2";
        break;
    }
    
    Flyway flyway = Flyway.configure(classLoader)
        .locations(location)
        .dataSource(dataSource())
        .load();
    
    flyway.migrate();
  }
  
  private DefaultConfiguration configuration() {
    DefaultConfiguration cfg = new DefaultConfiguration();
    cfg.set(MoreObjects.firstNonNull(config.getDatabaseDialect(), SQLDialect.H2));
    cfg.set(new DataSourceConnectionProvider(dataSource()));
    cfg.setExecutorProvider(this::getExecutor);
    return cfg;
  }
  
  public DSLContext dsl() {
    return new DefaultDSLContext(configuration());
  }
}
