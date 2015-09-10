package test;

import gobblin.configuration.WorkUnitState;
import gobblin.converter.DataConversionException;
import gobblin.converter.EmptyIterable;
import gobblin.converter.SchemaConversionException;
import gobblin.converter.SingleRecordIterable;
import gobblin.converter.ToAvroConverterBase;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * @see gobblin.converter.avro.JsonIntermediateToAvroConverter
 */
public class KafkaJsonConverter extends ToAvroConverterBase<String, byte[]>
{

    private static final Logger LOGGER = LoggerFactory.getLogger( KafkaJsonConverter.class );

    private static final String OUTPUT_SCHEMA = "writer.output.schema";

    private static JsonFactory jsonFactory = new JsonFactory();
    private static ObjectMapper mapper = new ObjectMapper( jsonFactory );

    private static TypeReference<HashMap<String,Object>> mapTypeRef = new TypeReference<HashMap<String,Object>>() {};

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public Schema convertSchema( String inputSchema, WorkUnitState workUnit ) throws SchemaConversionException
    {
        if ( LOGGER.isInfoEnabled() )
        {
            LOGGER.info( "Method Arguments [inputSchema={}, workUnit={}]", inputSchema, workUnit );
        }


        String outputSchema = workUnit.getProp(OUTPUT_SCHEMA);

        Schema outputSchemaInstance = new Schema.Parser().parse( outputSchema );

        return outputSchemaInstance;
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public Iterable<GenericRecord> convertRecord( Schema schema, byte[] inputRecord, WorkUnitState workUnit ) throws DataConversionException
    {
        if ( LOGGER.isInfoEnabled() )
        {
            LOGGER.info( "Method Arguments [schema={}, inputRecord={}, workUnit={}]", schema, inputRecord, workUnit );
        }

        HashMap<String,Object> values = null;

        try
        {
            values = mapper.readValue( inputRecord, mapTypeRef );
        }
        catch ( JsonParseException e )
        {
            LOGGER.error( "Skipping record as it cannot be converted:  " + new String( inputRecord ), e );
            return new EmptyIterable<>();
        }
        catch ( JsonMappingException e )
        {
            LOGGER.error( "Skipping record as it cannot be converted:  " + new String( inputRecord ), e );
            return new EmptyIterable<>();
        }
        catch ( IOException e )
        {
            throw new DataConversionException(e);
        }

        if ( values == null )
        {
            LOGGER.error( "Null mapper values" );
            return new EmptyIterable<>();
        }

        Set<String> fieldNames = getSchemaFieldNames( schema );

        GenericRecord record = new GenericData.Record( schema );
        for ( Map.Entry<String, Object> entry : values.entrySet() )
        {
            if ( fieldNames.contains( entry.getKey() ))
            {
                record.put( entry.getKey(), entry.getValue() );
            }
            else
            {
                LOGGER.error( "Skipping record as it cannot be converted:  " + new String( inputRecord ) );
                return new EmptyIterable<>();
            }


        }

        return new SingleRecordIterable<>( record );
    }

    private Set<String> getSchemaFieldNames( Schema schema )
    {
        List<org.apache.avro.Schema.Field> fields = schema.getFields();
        Set<String> fieldNames = new HashSet<>( fields.size() );

        for ( org.apache.avro.Schema.Field field : fields )
        {
            fieldNames.add( field.name() );
        }

        return fieldNames;
    }

}