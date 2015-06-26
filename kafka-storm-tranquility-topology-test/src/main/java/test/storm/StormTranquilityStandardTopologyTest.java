package test.storm;


import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;
import storm.kafka.trident.TransactionalTridentKafkaSpout;
import storm.kafka.trident.TridentKafkaConfig;
import storm.kafka.trident.TridentKafkaState;
import storm.kafka.trident.TridentKafkaStateFactory;
import storm.kafka.trident.TridentKafkaUpdater;
import storm.kafka.trident.mapper.TridentTupleToKafkaMapper;
import storm.kafka.trident.selector.DefaultTopicSelector;
import storm.trident.Stream;
import storm.trident.TridentTopology;
import storm.trident.tuple.TridentTuple;
import test.util.JsonUtils;
import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.tuple.Fields;

import com.fasterxml.jackson.core.JsonProcessingException;


/**
 * <pre><p>
 * {@link https://storm.apache.org/documentation/Trident-state}
 *   which links to:
 *   {@link https://github.com/apache/storm/blob/master/external/storm-kafka/src/jvm/storm/kafka/trident/TransactionalTridentKafkaSpout.java}
 *     which links to:
 *     {@link https://github.com/apache/storm/tree/master/external/storm-kafka}
 * </p></pre>
 *
 *
 */
@SuppressWarnings( "nls" )
public final class StormTranquilityStandardTopologyTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger( StormTranquilityStandardTopologyTest.class );

    private StormTranquilityStandardTopologyTest()
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

        // Set producer properties.
        Properties props = new Properties();
        props.put( "metadata.broker.list", AppConfiguration.KAFKA_SERVERS_CONFIG );
        props.put( "request.required.acks", AppConfiguration.KAFKA_SERVERS_CONFIG_PRODUCER_ORIGINAL_ACKS );
        props.put( "producer.type", "sync" );
        props.put( "serializer.class", "kafka.serializer.StringEncoder" );
        config.put( TridentKafkaState.KAFKA_BROKER_PROPERTIES, props );

        StormSubmitter.submitTopology( AppConfiguration.TOPOLOGY_NAME, config, buildTransactionalTopology() );
    }

    private static StormTopology buildTransactionalTopology()
    {
        TridentTopology topology = new TridentTopology();

        ZkHosts zkHosts = new ZkHosts( AppConfiguration.ZOOKEEPER_SERVERS_CONFIG );
        TridentKafkaConfig spoutConfig = new TridentKafkaConfig( zkHosts, AppConfiguration.KAFKA_SERVERS_TOPIC_PHASE1 );
        spoutConfig.scheme = new SchemeAsMultiScheme( new StringScheme() );

        Stream kafkaStream = topology.newStream( AppConfiguration.KAFKA_SPOUT_ID, new TransactionalTridentKafkaSpout( spoutConfig ) );

        Stream processingStream = kafkaStream
            .each(
                new Fields( StringScheme.STRING_SCHEME_KEY ),
                new MyNotificationDeserializerFunction(),
                new Fields( "notification" ) )
            .each(
                new Fields( "notification" ),
                new MyNotificationToMapFunction(),
                new Fields( "data" ) )
            ;

        TridentKafkaStateFactory stateFactory = new TridentKafkaStateFactory()
            .withKafkaTopicSelector( new DefaultTopicSelector( AppConfiguration.KAFKA_SERVERS_TOPIC_PHASE2 ) )
            .withTridentTupleToKafkaMapper( new MyStormTupleToKafkaMapper( null ) );

        Fields inputFields = new Fields("data");
        Fields functionFields = new Fields();

        processingStream.partitionPersist(
              stateFactory,
              inputFields,
              new TridentKafkaUpdater(),
              functionFields);

        return topology.build();

    }


    public static class MyStormTupleToKafkaMapper implements TridentTupleToKafkaMapper<String, String> {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings( "hiding" )
        private static final Logger LOGGER = LoggerFactory.getLogger( MyStormTupleToKafkaMapper.class );

        public final String keyFieldName;

        public MyStormTupleToKafkaMapper(String keyFieldName) {
            this.keyFieldName = keyFieldName;
        }

        @Override
        public String getKeyFromTuple(TridentTuple tuple) {

            if ( keyFieldName == null )
            {
                return null;
            }
            return String.valueOf( tuple.getValueByField( keyFieldName ) );
        }

        @Override
        public String getMessageFromTuple(TridentTuple tuple) {

            if ( tuple.getFields().size() != 1)
            {
                throw new IllegalArgumentException();
            }
            String fieldName = tuple.getFields().get( 0 );
            Object fieldValue = tuple.getValueByField( fieldName );

            @SuppressWarnings( "unchecked" )
            Map<String, Object> map = (Map<String, Object>)fieldValue;

            try
            {
                String output = JsonUtils.getObjectMapper().writeValueAsString( map );
                return output;
            }
            catch ( JsonProcessingException e )
            {
                LOGGER.error( "Unexpected exception!", e );
                throw new RuntimeException( e );
            }
        }
    }
}
