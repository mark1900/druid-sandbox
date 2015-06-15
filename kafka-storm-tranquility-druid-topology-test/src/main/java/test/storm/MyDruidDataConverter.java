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
public class MyDruidDataConverter extends BaseFunction
{

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger( MyDruidDataConverter.class );

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute( TridentTuple tuple, TridentCollector collector )
    {
        LOGGER.info( "Converting Message..." );

        try
        {
            MyNotification notification = (MyNotification)tuple.get( 0 );

            LOGGER.info( "Converted Message..." + String.valueOf( notification ) );

            List<Map<String, Object>> valueList = getDruidValues( notification );

            for ( Map<String, Object> valueMap : valueList )
            {
                collector.emit( new Values( valueMap ) );
            }

            LOGGER.info( "Emitted Values." + String.valueOf( notification ) );

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
    private static List<Map<String, Object>> getDruidValues( MyNotification notification )
    {
        List<MyEvent> observations = notification.getEvents();

        List<Map<String, Object>> valueList = new LinkedList<>();


        for ( MyEvent observation : observations )
        {
            Map<String, Object> valueMap = new LinkedHashMap<>();


            // The JSON serialization of your object must have a timestamp field in a format that Druid understands. By default,
            // Druid expects the field to be called "timestamp" and to be an ISO8601 timestamp.

//            LOGGER.info( "Timestamp1:" + DruidDateUtil.fromDate( new DateTime(System.currentTimeMillis(), DateTimeZone.UTC).toDate() ) );
//            LOGGER.info( "Timestamp2:" + DruidDateUtil.fromDateTime( new DateTime(System.currentTimeMillis(), DateTimeZone.UTC) ) );

            valueMap.put( MyDruidColumns.TIMESTAMP, DruidDateUtil.fromDate( new DateTime( System.currentTimeMillis(), DateTimeZone.UTC ).toDate() ) );

            valueMap.put( MyDruidColumns.VERSION, notification.getVersion() );
            valueMap.put( MyDruidColumns.TYPE, observation.getType() );
            valueMap.put( MyDruidColumns.LEVEL, observation.getLevel() );
            valueMap.put( MyDruidColumns.MESSAGE, observation.getMessage() );
            valueList.add( valueMap );
        }

        return valueList;
    }

}