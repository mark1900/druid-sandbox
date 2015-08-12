package test.storm;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;
import test.data.MyNotification;
import test.util.JsonUtils;
import backtype.storm.tuple.Values;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

@SuppressWarnings( "nls" )
public class MyNotificationDeserializerFunction extends BaseFunction
{

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger( MyNotificationDeserializerFunction.class );

    public static final int INPUT_FIELD_INDEX = 0;
    public static final int INPUT_FIELD_COUNT = 1;

    public static final int OUTPUT_FIELD_INDEX = 0;
    public static final int OUTPUT_FIELD_COUNT = 1;

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute( TridentTuple tuple, TridentCollector collector )
    {
        if ( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug( "Deserializing..." );
        }

        try
        {
            String message = tuple.getString( INPUT_FIELD_INDEX );

            MyNotification notification =
                JsonUtils.getObjectMapper().readValue( message, MyNotification.class );

            if ( LOGGER.isInfoEnabled() )
            {
                LOGGER.info( "Deserialized:  " + String.valueOf( notification ) );
            }

            collector.emit( new Values( notification ) );

        }
        catch ( JsonParseException e )
        {
            LOGGER.error( "Unexpected Exception!", e );
        }
        catch ( JsonMappingException e )
        {
            LOGGER.error( "Unexpected Exception!", e );
        }
        catch ( IOException e )
        {
            LOGGER.error( "Unexpected Exception!", e );
        }
        catch ( RuntimeException e )
        {
            LOGGER.error( "Unexpected Exception!", e );
        }

    }

}