package test.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;

public final class JsonUtils
{

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule( new JodaModule() )
        .configure( DeserializationFeature.UNWRAP_ROOT_VALUE, false )
        .configure( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false );

    private JsonUtils()
    {
        //NO-OP
    }

    public static ObjectMapper getObjectMapper()
    {
        return MAPPER;
    }

}
