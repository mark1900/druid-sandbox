package org.skife.jdbi.v2;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.exceptions.UnableToCreateStatementException;
import org.skife.jdbi.v2.tweak.RewrittenStatement;

import io.druid.metadata.SQLMetadataStorageActionHandler;
import io.druid.metadata.storage.sqlserver.SQLServerConnector.CustomStatementRewriter;
import junit.framework.Assert;

@SuppressWarnings( "nls" )
public class CustomStatementRewriterTest
{

    private CustomStatementRewriter customStatementRewriter;
    private Binding params;
    private StatementContext ctx;

    @Before
    public void setUp()
    {
        customStatementRewriter = new CustomStatementRewriter();

        params = new Binding();

        Map<String, Object> globalAttributes = new LinkedHashMap<>();
        ctx = new ConcreteStatementContext(globalAttributes);
    }


    private String rewrite(String sql)
    {
        RewrittenStatement rewrittenStatement = customStatementRewriter.rewrite(sql, params, ctx);
        return rewrittenStatement.getSql();
    }

    /**
    *
    * @see org.skife.jdbi.v2.TestColonStatementRewriter
    * @see "https://github.com/jdbi/jdbi/blob/master/src/test/java/org/skife/jdbi/v2/TestColonStatementRewriter.java"
    *
    */
   @Test
   public void testCustomStatementRewriter()
   {

       Assert.assertEquals("select column# from table1 where id = ?", rewrite("select column# from table1 where id = :id"));

       Assert.assertEquals("select * from table2\n where id = ?", rewrite("select * from table2\n where id = :id"));

       try
       {
           rewrite("select * from table3 where id = :\u0091\u009c");  // Control codes - https://en.wikipedia.org/wiki/List_of_Unicode_characters
           Assert.fail("Expected 'UnableToCreateStatementException'");
       }
       catch(@SuppressWarnings( "unused" ) UnableToCreateStatementException e)
       {
           //
       }

   }

   /**
    *
    * @see io.druid.metadata.SQLMetadataConnector#createTable(String, Iterable)
    *
    */
   @Test
   public void testSQLMetadataConnectorCreateTable()
   {
       String sqlIn = "CREATE TABLE %1$s (\n"
               + "  id VARCHAR(255) NOT NULL,\n"
               + "  dataSource VARCHAR(255) NOT NULL,\n"
               + "  created_date VARCHAR(255) NOT NULL,\n"
               + "  start VARCHAR(255) NOT NULL,\n"
               + "  \"end\" VARCHAR(255) NOT NULL,\n"
               + "  partitioned BOOLEAN NOT NULL,\n"
               + "  version VARCHAR(255) NOT NULL,\n"
               + "  used BOOLEAN NOT NULL,\n"
               + "  payload %2$s NOT NULL,\n"
               + "  PRIMARY KEY (id)\n"
               + ")";

       String sqlOut = "CREATE TABLE %1$s (\n" +
               "  id VARCHAR(255) NOT NULL,\n" +
               "  dataSource VARCHAR(255) NOT NULL,\n" +
               "  created_date VARCHAR(255) NOT NULL,\n" +
               "  start VARCHAR(255) NOT NULL,\n" +
               "  \"end\" VARCHAR(255) NOT NULL,\n" +
               "  partitioned BIT NOT NULL,\n" +
               "  version VARCHAR(255) NOT NULL,\n" +
               "  used BIT NOT NULL,\n" +
               "  payload %2$s NOT NULL,\n" +
               "  PRIMARY KEY (id)\n" +
               ")";

       Assert.assertEquals( sqlOut, rewrite(sqlIn) );

   }

   /**
    *
    * @see io.druid.metadata.SQLMetadataStorageActionHandler#setStatus(String, boolean, Object)
    *
    */
   @Test
   public void testSQLMetadataStorageActionHandlerSetStatus()
   {
       Assert.assertEquals("UPDATE %s SET active = ?, status_payload = ? WHERE id = ? AND active = 1",
           rewrite("UPDATE %s SET active = :active, status_payload = :status_payload WHERE id = :id AND active = TRUE"));


   }

   /**
   *
   * @see io.druid.metadata.SQLMetadataStorageActionHandler#getInactiveStatusesSince(org.joda.time.DateTime)
   *
   */
  @Test
  public void testSQLMetadataStorageActionHandlerGetInactiveStatusesSince()
  {
      Assert.assertEquals("SELECT id, status_payload FROM %s WHERE active = 0 AND created_date >= ? ORDER BY created_date DESC",
          rewrite("SELECT id, status_payload FROM %s WHERE active = FALSE AND created_date >= :start ORDER BY created_date DESC"));
  }

}
