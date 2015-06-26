package test.storm;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;
import test.data.MyEvent;
import test.data.MyNotification;
import test.util.DruidDateUtil;
import backtype.storm.tuple.Values;

@SuppressWarnings( "nls" )
public class MyNotificationToMapFunction extends BaseFunction
{

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger( MyNotificationToMapFunction.class );

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
            LOGGER.debug( "Converting..." );
        }

        try
        {
            MyNotification notification = (MyNotification)tuple.get( INPUT_FIELD_INDEX );

            List<Map<String, Object>> valueList = getMyDataFieldValues( notification );

            if ( LOGGER.isDebugEnabled() )
            {
                LOGGER.debug( "Converted:  " + String.valueOf( valueList ) );
            }

            for ( Map<String, Object> valueMap : valueList )
            {
                collector.emit( new Values( valueMap ) );
            }

            if ( LOGGER.isInfoEnabled() )
            {
                LOGGER.info( "Emitted Value Count:  " + valueList.size() );
            }

        }
        catch ( RuntimeException e )
        {
            LOGGER.error( "Unexpected Exception!", e );
        }

    }

    /**
     * <pre>
     * Druid requires that the "timestamp" field be in ISO8601 format.  See {@link https://github.com/druid-io/tranquility}
     *
     * </pre>
     *
     * @param notification
     * @return
     */
    private static List<Map<String, Object>> getMyDataFieldValues( MyNotification notification )
    {
        List<MyEvent> observations = notification.getEvents();

        List<Map<String, Object>> valueList = new LinkedList<>();


        for ( MyEvent observation : observations )
        {
            Map<String, Object> valueMap = new LinkedHashMap<>();


            // The JSON serialization of your object must have a timestamp field in a format that Druid understands.
            // By default, Druid expects the field to be called "timestamp" and to be an ISO8601 timestamp.

//            LOGGER.info( "Timestamp1:" + DruidDateUtil.fromDate( new DateTime(System.currentTimeMillis(), DateTimeZone.UTC).toDate() ) );
//            LOGGER.info( "Timestamp2:" + DruidDateUtil.fromDateTime( new DateTime(System.currentTimeMillis(), DateTimeZone.UTC) ) );

            valueMap.put( MyDataFieldNames.TIMESTAMP, DruidDateUtil.fromDate( new DateTime( System.currentTimeMillis(), DateTimeZone.UTC ).toDate() ) );

            valueMap.put( MyDataFieldNames.VERSION, notification.getVersion() );
            valueMap.put( MyDataFieldNames.TYPE, observation.getType() );
            valueMap.put( MyDataFieldNames.LEVEL, observation.getLevel() );
            valueMap.put( MyDataFieldNames.MESSAGE, observation.getMessage() );
            valueList.add( valueMap );
        }

        return valueList;
    }

}