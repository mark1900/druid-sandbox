package test;

import gobblin.configuration.WorkUnitState;
import gobblin.converter.DataConversionException;
import gobblin.converter.SchemaConversionException;
import gobblin.converter.SingleRecordIterable;
import gobblin.converter.ToAvroConverterBase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class KafkaJsonConverter extends ToAvroConverterBase<String, String>
{

    private static final Logger LOGGER = LoggerFactory.getLogger( KafkaJsonConverter.class );

    private static JsonFactory jsonFactory = new JsonFactory();
    private static ObjectMapper mapper = new ObjectMapper( jsonFactory );

    private static TypeReference<HashMap<String,Object>> mapTypeRef = new TypeReference<HashMap<String,Object>>() {};

    @Override
    public Schema convertSchema( String inputSchema, WorkUnitState workUnit ) throws SchemaConversionException
    {
        LOGGER.info( "## KafkaJsonConverter.convertSchema(..) -> " );

        return new Schema.Parser().parse( inputSchema );
    }

    @Override
    public Iterable<GenericRecord> convertRecord( Schema schema, String inputRecord, WorkUnitState workUnit ) throws DataConversionException
    {

        LOGGER.info( "## KafkaJsonConverter.convertRecord(..) -> " );

        HashMap<String,Object> values = null;

        try
        {
            values = mapper.readValue( inputRecord, mapTypeRef );
        }
        catch ( JsonParseException e )
        {
            throw new DataConversionException(e);
        }
        catch ( JsonMappingException e )
        {
            throw new DataConversionException(e);
        }
        catch ( IOException e )
        {
            throw new DataConversionException(e);
        }

        GenericRecord record = new GenericData.Record( schema );
        for ( Map.Entry<String, Object> entry : values.entrySet() )
        {
            record.put( entry.getKey(), entry.getValue() );
        }

        return new SingleRecordIterable<>( record );
    }

}