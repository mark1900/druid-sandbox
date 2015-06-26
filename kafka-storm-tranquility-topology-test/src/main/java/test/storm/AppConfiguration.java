package test.storm;


/**
 *
 */
@SuppressWarnings( "nls" )
public final class AppConfiguration
{

    public static final String TOPOLOGY_NAME = "kafka-storm-tranquility-topology-test";
    public static final String KAFKA_SPOUT_ID = "kafka-storm-tranquility-topology-test-kafka-spout";

    public static final String ZOOKEEPER_SERVERS_CONFIG = System.getProperty(
        "test.storm.topology.zookeeper.servers.config", "localhost:2181" );

    public static final String KAFKA_SERVERS_CONFIG = System.getProperty(
        "test.storm.topology.kafka.servers.config", "localhost:9092" );

    public static final String KAFKA_SERVERS_CONFIG_ACKS = System.getProperty(
        "test.storm.topology.kafka.servers.config.acks", "all" );
    public static final Integer KAFKA_SERVERS_CONFIG_RETRIES = Integer.getInteger(
        "test.storm.topology.kafka.servers.config.retries", 100);
    public static final Integer KAFKA_SERVERS_CONFIG_RETRY_BACKOFF = Integer.getInteger(
        "test.storm.topology.kafka.servers.config.retry.backoff", 1000 );

    public static final String KAFKA_SERVERS_TOPIC_PHASE1 = System.getProperty(
        "test.storm.topology.kafka.servers.config.topic.phase1", TOPOLOGY_NAME + "_v1" + "_phase1" );

    public static final String KAFKA_SERVERS_TOPIC_PHASE2 = System.getProperty(
        "test.storm.topology.kafka.servers.config.topic.phase2", TOPOLOGY_NAME + "_v1" + "_phase2" );

    public static final long KAFKA_CLIENT_REQUEST_TIMEOUT = Long.getLong(
        "test.storm.topology.kafka.client.request.timeout", 60 * 1000 );


    private AppConfiguration()
    {
        //
    }
}
