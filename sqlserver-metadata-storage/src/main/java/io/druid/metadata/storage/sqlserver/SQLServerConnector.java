package io.druid.metadata.storage.sqlserver;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.dbcp2.BasicDataSource;
import org.skife.jdbi.v2.Binding;
import org.skife.jdbi.v2.ColonPrefixNamedParamStatementRewriter;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.tweak.RewrittenStatement;
import org.skife.jdbi.v2.tweak.StatementRewriter;
import org.skife.jdbi.v2.util.StringMapper;

import com.google.common.base.Supplier;
import com.google.inject.Inject;
import com.metamx.common.logger.Logger;

import io.druid.metadata.MetadataStorageConnectorConfig;
import io.druid.metadata.MetadataStorageTablesConfig;
import io.druid.metadata.SQLMetadataConnector;

@SuppressWarnings( "nls" )
public class SQLServerConnector extends SQLMetadataConnector
{
  private static final Logger log = new Logger(SQLServerConnector.class);

  private static final String PAYLOAD_TYPE = "VARBINARY(MAX)";
  private static final String SERIAL_TYPE = "[int] IDENTITY (1, 1)";

  public static final int DEFAULT_STREAMING_RESULT_SIZE = 100;

  private final DBI dbi;


  private final Set<String> TRANSIENT_SQL_CLASS_CODES = new HashSet<>(Arrays.asList(
    "08", "53", "54", "57", "58",   // Resource Failures
    "JW", "JZ", "S1",               // Transient Failures
    "40"                            // Transaction Rollback
  ));

  @Inject
  public SQLServerConnector(Supplier<MetadataStorageConnectorConfig> config, Supplier<MetadataStorageTablesConfig> dbTables)
  {
    super(config, dbTables);

    final BasicDataSource datasource = getDatasource();
    datasource.setDriverClassLoader(getClass().getClassLoader());
    datasource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

    this.dbi = new DBI(datasource);

    this.dbi.setStatementRewriter( new CustomStatementRewriter() );

    log.info("Configured Sql Server as metadata storage");
  }

  public static class CustomStatementRewriter implements StatementRewriter
  {
      @Override
    public RewrittenStatement rewrite(String sql, Binding params, StatementContext ctx)
    {

      String sqlUpdated = sql
        .replaceAll( "(?i)BOOLEAN NOT NULL DEFAULT FALSE", "BIT NOT NULL DEFAULT (0)" )
        .replaceAll( "(?i)BOOLEAN NOT NULL DEFAULT TRUE", "BIT NOT NULL DEFAULT (1)" )
        .replaceAll( "(?i)BOOLEAN DEFAULT FALSE", "BIT NOT NULL DEFAULT (0)" )
        .replaceAll( "(?i)BOOLEAN DEFAULT TRUE", "BIT NOT NULL DEFAULT (1)" )
        .replaceAll( "(?i)BOOLEAN", "BIT" )
        .replaceAll( "(?i)TRUE", "1" )
        .replaceAll( "(?i)FALSE", "0" )
        ;
      return ( new ColonPrefixNamedParamStatementRewriter() ).rewrite( sqlUpdated, params, ctx );
    }
  }

  @Override
  protected String getPayloadType() {
    return PAYLOAD_TYPE;
  }

  @Override
  protected String getSerialType()
  {
    return SERIAL_TYPE;
  }

//  @Override
//  protected int getStreamingFetchSize()
//  {
//    return DEFAULT_STREAMING_RESULT_SIZE;
//  }

  @Override
  public boolean tableExists(final Handle handle, final String tableName)
  {
    return !handle.createQuery("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = :tableName")
      .bind("tableName", tableName)
      .map(StringMapper.FIRST)
      .list()
      .isEmpty();
  }

  /**
   *
   * @see "http://stackoverflow.com/questions/1197733/does-sql-server-offer-anything-like-mysqls-on-duplicate-key-update"
   *
   * {@inheritDoc}
   */
  @Override
  public Void insertOrUpdate(
      final String tableName,
      final String keyColumn,
      final String valueColumn,
      final String key,
      final byte[] value
  ) throws Exception
  {
    return getDBI().withHandle(
      new HandleCallback<Void>()
      {
        @Override
        public Void withHandle(Handle handle) throws Exception
        {
          handle.createStatement( String.format(
              "MERGE INTO %1$s WITH (UPDLOCK, HOLDLOCK) as target"
              + " USING "
              + " (:key, :value) as source (%2$s, %3$s)"
              + " ON"
              + " (target.%2$s = source.%2$s)"
              + " WHEN MATCHED THEN UPDATE SET %3$s = :value"
              + " WHEN NOT MATCHED THEN INSERT (%2$s, %3$s) VALUES (:key, :value)",
              tableName,
              keyColumn,
              valueColumn )
            )
            .bind("key", key)
            .bind("value", value)
            .execute();

          return null;
        }
      }
    );
  }

    @Override
    public DBI getDBI()
    {
      return dbi;
    }

  /**
   *
   * @see java.sql.SQLException#getSQLState()
   * @see "https://github.com/spring-projects/spring-framework/blob/master/spring-jdbc/src/main/java/org/springframework/jdbc/support/SQLStateSQLExceptionTranslator.java"
   *
   * {@inheritDoc}
   */
  @Override
  protected boolean connectorIsTransientException(Throwable e)
  {
    if(e instanceof SQLException)
    {
      final String sqlState = ((SQLException) e).getSQLState();
      if ( sqlState == null )
      {
        return false;
      }

      final String sqlClassCode = sqlState.substring(0, 2);
      if ( TRANSIENT_SQL_CLASS_CODES.contains( sqlClassCode ))
      {
        return true;
      }
    }
    return false;
  }
}
