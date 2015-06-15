package test.storm;

import io.druid.data.input.impl.TimestampSpec;
import io.druid.granularity.QueryGranularity;
import io.druid.query.aggregation.AggregatorFactory;
import io.druid.query.aggregation.CountAggregatorFactory;
import io.druid.query.aggregation.LongSumAggregatorFactory;

import java.util.List;
import java.util.Map;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.BoundedExponentialBackoffRetry;
import org.apache.storm.guava.collect.ImmutableList;
import org.joda.time.DateTime;
import org.joda.time.Period;

import test.util.DruidDateUtil;
import backtype.storm.task.IMetricsContext;

import com.metamx.common.Granularity;
import com.metamx.tranquility.beam.Beam;
import com.metamx.tranquility.beam.ClusteredBeamTuning;
import com.metamx.tranquility.druid.DruidBeams;
import com.metamx.tranquility.druid.DruidDimensions;
import com.metamx.tranquility.druid.DruidEnvironment;
import com.metamx.tranquility.druid.DruidLocation;
import com.metamx.tranquility.druid.DruidRollup;
import com.metamx.tranquility.storm.BeamFactory;
import com.metamx.tranquility.typeclass.Timestamper;

/**
 *
 *
 * https://github.com/druid-io/tranquility#storm
 * https://groups.google.com/forum/#!topic/druid-development/VV0jKRTzRc4
 *
 */
@SuppressWarnings( "nls" )
public class MyTranquilityBeamFactory implements BeamFactory<Map<String, Object>>
{

    private static final long serialVersionUID = 1L;

//    private static final Logger LOGGER = LoggerFactory.getLogger( MyDruidDataConverter.class );

    @SuppressWarnings( { "resource", "unused" } )
    @Override
    public Beam<Map<String, Object>> makeBeam( Map<?, ?> config, IMetricsContext metrics )
    {

        String zkHostPort = AppConfiguration.ZOOKEEPER_SERVERS_CONFIG;
        String discoveryPath = AppConfiguration.DRUID_OVERLORD_DISCOVERY_CURATOR_PATH;
        String dataSource = AppConfiguration.DRUID_DATASOURCE;

        final List<String> dimensions = ImmutableList.of(
            MyDruidColumns.VERSION,
            MyDruidColumns.TYPE,
            MyDruidColumns.MESSAGE
            );
        final List<AggregatorFactory> aggregators = ImmutableList
            .<AggregatorFactory> of(
                new CountAggregatorFactory( "count" ),
                new LongSumAggregatorFactory( MyDruidColumns.LEVEL, MyDruidColumns.LEVEL )
            );

        CuratorFramework curator = CuratorFrameworkFactory.newClient( zkHostPort, new BoundedExponentialBackoffRetry( 100, 1000, 5 ) );
        curator.start();

        return DruidBeams
            .builder(
                new Timestamper<Map<String, Object>>() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public DateTime timestamp( Map<String, Object> theMap )
                    {
                        return DruidDateUtil.toDateTime( (String)theMap.get( "timestamp" ) );
                    }
                }
            )
            .curator( curator )
            .discoveryPath( discoveryPath )
            .location( new DruidLocation( new DruidEnvironment( "overlord", "druid:firehose:%s" ), dataSource ) )
            .timestampSpec( new TimestampSpec( "timestamp", "iso" ) )  // Default DruidBeams DefaultTimestampSpec value
            .rollup( DruidRollup.create( DruidDimensions.specific( dimensions ), aggregators, QueryGranularity.NONE ) )
            .tuning( ClusteredBeamTuning.create( Granularity.HOUR, new Period( "PT2M" ), new Period( "PT0M" ), 1, 1 ) )
            .buildBeam();

    }
}