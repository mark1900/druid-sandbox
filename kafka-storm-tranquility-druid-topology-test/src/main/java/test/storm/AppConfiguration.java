package test.storm;

/**
 *
 */
@SuppressWarnings( "nls" )
public final class AppConfiguration
{

    public static final String TOPOLOGY_NAME = "kafka-storm-tranquility-druid-topology-test";
    public static final String KAFKA_SPOUT_ID = "kafka-storm-tranquility-druid-topology-test-kafka-spout";

    public static final String ZOOKEEPER_SERVERS_CONFIG = System.getProperty(
        "test.storm.topology.zookeeper.servers.config", "127.0.0.1:2181" );

    public static final String KAFKA_SERVERS_CONFIG = System.getProperty(
        "test.storm.topology.kafka.servers.config", "127.0.0.1:9092" );
    public static final String KAFKA_SERVERS_TOPIC = System.getProperty(
        "test.storm.topology.kafka.servers.config.topic", TOPOLOGY_NAME + "_v1" );

    public static final long KAFKA_CLIENT_REQUEST_TIMEOUT = Long.getLong(
        "test.storm.topology.kafka.client.request.timeout", 60 * 1000 );

    public static final String DRUID_OVERLORD_DISCOVERY_CURATOR_PATH = System.getProperty(
        "test.storm.topology.druid.discovery.curator.path", "/druid/discovery" );

    public static final String DRUID_DATASOURCE = System.getProperty(
        "test.storm.topology.druid.datasource", "kafka-storm-tranquility-druid-topology-test-datasource" );

    private AppConfiguration()
    {
        //
    }
}
