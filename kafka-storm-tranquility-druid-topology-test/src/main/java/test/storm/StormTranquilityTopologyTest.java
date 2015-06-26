package test.storm;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;
import storm.kafka.trident.TransactionalTridentKafkaSpout;
import storm.kafka.trident.TridentKafkaConfig;
import storm.trident.Stream;
import storm.trident.TridentTopology;
import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.tuple.Fields;

import com.metamx.tranquility.storm.TridentBeamStateFactory;
import com.metamx.tranquility.storm.TridentBeamStateUpdater;

@SuppressWarnings( "nls" )
public final class StormTranquilityTopologyTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger( StormTranquilityTopologyTest.class );

    private StormTranquilityTopologyTest()
    {
        //
    }

    public static void main( String[] args ) throws Exception
    {
        LOGGER.info( "Creating topology: " + AppConfiguration.TOPOLOGY_NAME );
        submitTransactionalTopology();
        LOGGER.info( "Submitted topology: " + AppConfiguration.TOPOLOGY_NAME );
    }

    private static void submitTransactionalTopology() throws Exception
    {

        Config config = new Config();
        config.put( Config.TOPOLOGY_DEBUG, true );

        StormSubmitter.submitTopology( AppConfiguration.TOPOLOGY_NAME, config, buildTransactionalTopology() );
    }

    private static StormTopology buildTransactionalTopology()
    {
        TridentTopology topology = new TridentTopology();
        Stream kafkaStream = topology.newStream( AppConfiguration.KAFKA_SPOUT_ID, buildTransactionalKafkaSpout() );

        Stream processingStream = kafkaStream
            .each(
                new Fields( StringScheme.STRING_SCHEME_KEY ),
                new MyNotificationDeserializerFunction(),
                new Fields( "notification" ) )
            .each(
                new Fields( "notification" ),
                new MyNotificationToMapFunction(),
                new Fields( "data" ) );


        Fields inputFields = new Fields( "data" );

        processingStream.partitionPersist(
                new TridentBeamStateFactory<>( new MyTranquilityBeamFactory() ),
                inputFields,
                new TridentBeamStateUpdater<Map<String, Object>>()
             );

        return topology.build();

    }

    private static TransactionalTridentKafkaSpout buildTransactionalKafkaSpout()
    {
        String zkHostPort = AppConfiguration.ZOOKEEPER_SERVERS_CONFIG;
        String topic = AppConfiguration.KAFKA_SERVERS_TOPIC;

        ZkHosts zkHosts = new ZkHosts( zkHostPort );

        TridentKafkaConfig spoutConfig = new TridentKafkaConfig( zkHosts, topic );
        spoutConfig.scheme = new SchemeAsMultiScheme( new StringScheme() );

        return new TransactionalTridentKafkaSpout( spoutConfig );
    }

}
