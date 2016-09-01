package io.druid.metadata.storage.sqlserver;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Key;
import io.druid.guice.LazySingleton;
import io.druid.guice.PolyBind;
import io.druid.guice.SQLMetadataStorageDruidModule;
import io.druid.initialization.DruidModule;
import io.druid.metadata.MetadataStorageConnector;
import io.druid.metadata.SQLMetadataConnector;

import java.util.List;

public class SQLServerMetadataStorageModule extends SQLMetadataStorageDruidModule implements DruidModule
{

  public static final String TYPE = "sqlserver";

  public SQLServerMetadataStorageModule()
  {
    super(TYPE);
  }

  @Override
  public List<? extends com.fasterxml.jackson.databind.Module> getJacksonModules()
  {
    return ImmutableList.of();
  }

  @Override
  public void configure(Binder binder)
  {
    super.configure(binder);

    PolyBind.optionBinder(binder, Key.get(MetadataStorageConnector.class))
            .addBinding(TYPE)
            .to(SQLServerConnector.class)
            .in(LazySingleton.class);

    PolyBind.optionBinder(binder, Key.get(SQLMetadataConnector.class))
            .addBinding(TYPE)
            .to(SQLServerConnector.class)
            .in(LazySingleton.class);
  }
}
