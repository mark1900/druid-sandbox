package test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 */
public class AppConfiguration
{

    public static final String KAFKA_SERVERS_CONFIG;

    public static final String KAFKA_SERVERS_TOPIC;


    private static Properties properties = new Properties();

    static {
        try ( InputStream in = AppConfiguration.class.getResourceAsStream( "/kafka-gobblin-hdfs-test.pull" ) )
        {
            properties.load(in);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        KAFKA_SERVERS_CONFIG = System.getProperty(
            "kafka-gobblin-hdfs-test.kafka.servers.config",
            getGobblinProperty( "kafka.brokers", "localhost:6667" ) );

        KAFKA_SERVERS_TOPIC = System.getProperty(
            "kafka-gobblin-hdfs-test.kafka.servers.config.topic.test", "kafka-gobblin-hdfs-test" );
    }

    private AppConfiguration()
    {
        // NOOP
    }

    public static String getGobblinProperty(String key)
    {
        return properties.getProperty(key);
    }

    public static String getGobblinProperty(String key, String defaultValue)
    {
        return properties.getProperty(key, defaultValue);
    }

    public static void main( String[] args )
    {
        System.out.println( "\n\n-- System Properties --\n\n" );
        System.getProperties().list( System.out );

        System.out.println( "\n\n-- Gobblin Properties --\n\n" );
        properties.list( System.out );
    }
}
