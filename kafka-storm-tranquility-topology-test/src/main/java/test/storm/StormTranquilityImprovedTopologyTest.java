package test.storm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;
import storm.kafka.trident.TransactionalTridentKafkaSpout;
import storm.kafka.trident.TridentKafkaConfig;
import storm.trident.Stream;
import storm.trident.TridentTopology;
import storm.trident.operation.TridentCollector;
import storm.trident.state.BaseStateUpdater;
import storm.trident.state.State;
import storm.trident.state.StateFactory;
import storm.trident.tuple.TridentTuple;
import test.util.JsonUtils;
import test.util.KafkaValueSenderSvc;
import test.util.KafkaValueSenderSvcImpl;
import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.task.IMetricsContext;
import backtype.storm.tuple.Fields;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * <pre>
 * <p>
 * {@link https://storm.apache.org/documentation/Trident-state}
 *   which links to:
 *   {@link https://github.com/apache/storm/blob/master/external/storm-kafka/src/jvm/storm/kafka/trident/TransactionalTridentKafkaSpout.java}
 *     which links to:
 *     {@link https://github.com/apache/storm/tree/master/external/storm-kafka}
 * </p>
 * </pre>
 * <pre>
 * <p>
 * Storm Real-time Processing Cookbook - Chapter 7. Real-time Machine Learning - Operational classification of transactional streams using Random Forest
 *   which links to:
 *   {@link https://github.com/quintona/trident-kafka-push}
 *     which links to:
 *       {@link https://github.com/quintona/trident-kafka-push/blob/master/src/main/java/com/github/quintona/KafkaState.java}
 *         which compares to:
 *           {@link https://github.com/apache/storm/blob/master/external/storm-kafka/src/jvm/storm/kafka/trident/TridentKafkaState.java}
 * </p>
 * </pre>
 *
 */
@SuppressWarnings( "nls" )
public final class StormTranquilityImprovedTopologyTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger( StormTranquilityImprovedTopologyTest.class );

    private StormTranquilityImprovedTopologyTest()
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
                    new Fields( "output" ) );

        MyTridentKafkaStateFactory stateFactory = new MyTridentKafkaStateFactory( AppConfiguration.KAFKA_SERVERS_CONFIG, AppConfiguration.KAFKA_SERVERS_TOPIC_PHASE2, true );

        Fields kafkaInputFields = new Fields( "output" );
        Fields kafkaFunctionFields = new Fields();
        processingStream.partitionPersist( stateFactory, kafkaInputFields, new MyTridentKafkaStateUpdater(), kafkaFunctionFields );

        return topology.build();

    }

    /**
    *
    * @see storm.kafka.trident.TridentKafkaStateFactory
    */
    public static class MyTridentKafkaStateFactory implements StateFactory
    {

        private static final long serialVersionUID = 1L;

        private String servers;
        private String topic;
        boolean transactional;

        public MyTridentKafkaStateFactory( String servers, String topic, boolean transactional )
        {
            this.servers = servers;
            this.topic = topic;
            this.transactional = transactional;
        }

        @SuppressWarnings( { "unused", "rawtypes" } )
        @Override
        public State makeState( Map conf, IMetricsContext metrics, int partitionIndex, int numPartitions )
        {
            return new MyTridentKafkaState( servers, topic, transactional );
        }

    }

    /**
    *
    * @see storm.kafka.trident.TridentKafkaState
    */
    public static class MyTridentKafkaState implements State
    {

        @SuppressWarnings( { "hiding"} )
        private static final Logger LOGGER = LoggerFactory.getLogger( MyTridentKafkaState.class );

        ConcurrentLinkedQueue<String> messages = new ConcurrentLinkedQueue<>();

        @SuppressWarnings( "unused" )
        private String servers;
        private String topic;
        private boolean transactional;

        private KafkaValueSenderSvc kafkaValueSenderSvc;

        public MyTridentKafkaState( String servers, String topic, boolean transactional )
        {
            this.servers = servers;
            this.topic = topic;
            this.transactional = transactional;

            KafkaValueSenderSvcImpl kafkaValueSenderSvcImpl = new KafkaValueSenderSvcImpl();
            kafkaValueSenderSvcImpl.init( servers );
            kafkaValueSenderSvcImpl.addShutdownHook();
            kafkaValueSenderSvc = kafkaValueSenderSvcImpl;
        }

        @Override
        public void beginCommit( @SuppressWarnings( "unused" ) Long txid )
        {
            if ( messages.size() > 0 )
            {
                throw new RuntimeException( "Unexpected KafkaState" );
            }
        }

        public void updateState(List<TridentTuple> tuples, @SuppressWarnings( "unused" ) TridentCollector collector)
        {
            for ( TridentTuple tuple : tuples )
            {
                if ( tuple.size() > 0 )
                {
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
                        enqueue( output );
                    }
                    catch ( JsonProcessingException e )
                    {
                        LOGGER.error( "Unexpected exception!", e );
                        throw new RuntimeException( e );
                    }
                }
            }
        }

        public void enqueue( String message )
        {
            if ( transactional )
            {
                messages.add( message );
            }
            else
            {
                kafkaValueSenderSvc.send( topic, message );
            }
        }

        @Override
        public void commit( @SuppressWarnings( "unused" ) Long txid )
        {

            if ( messages.size() < 1 )
            {
                return;
            }

            List<String> messageBatch = new ArrayList<>( messages.size() );

            String message = messages.poll();
            while ( message != null )
            {
                messageBatch.add( message );
                message = messages.poll();
            }

            try
            {
                kafkaValueSenderSvc.sendAndWait( topic, messageBatch, null );
            }
            catch( Exception e )
            {
                LOGGER.error( "Unexpected Exception!", e );
            }

        }

    }

    /**
     *
     * @see storm.kafka.trident.TridentKafkaUpdater
     */
    public static class MyTridentKafkaStateUpdater extends BaseStateUpdater<MyTridentKafkaState>
    {

        private static final long serialVersionUID = 1L;

        public MyTridentKafkaStateUpdater()
        {
            //
        }

        @Override
        public void updateState( MyTridentKafkaState state, List<TridentTuple> tuples, TridentCollector collector )
        {
            state.updateState( tuples, collector );
        }

    }

}
