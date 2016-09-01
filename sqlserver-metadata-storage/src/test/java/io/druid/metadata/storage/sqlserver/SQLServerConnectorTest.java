package io.druid.metadata.storage.sqlserver;

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Suppliers;

import io.druid.metadata.MetadataStorageConnectorConfig;
import io.druid.metadata.MetadataStorageTablesConfig;

@SuppressWarnings( "nls" )
public class SQLServerConnectorTest
{


  @Test
  public void testIsTransientException() throws Exception
  {
    SQLServerConnector connector = new SQLServerConnector(
        Suppliers.ofInstance(new MetadataStorageConnectorConfig()),
        Suppliers.ofInstance(
            new MetadataStorageTablesConfig(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            )
        )
    );

    Assert.assertTrue(connector.isTransientException(new SQLException("Resource Failure!", "08DIE")));
    Assert.assertTrue(connector.isTransientException(new SQLException("Resource Failure as well!", "53RES")));
    Assert.assertTrue(connector.isTransientException(new SQLException("Transient Failures", "JW001")));
    Assert.assertTrue(connector.isTransientException(new SQLException("Transient Rollback", "40001")));

    Assert.assertFalse(connector.isTransientException(new SQLException("SQLException with reason only")));
    Assert.assertFalse(connector.isTransientException(new SQLException()));
    Assert.assertFalse(connector.isTransientException(new Exception("Exception with reason only")));
    Assert.assertFalse(connector.isTransientException(new Throwable("Throwable with reason only")));
  }

}
