package test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 */
public class AppConfiguration
{

    public static final String KAFKA_SERVERS_CONFIG = System.getProperty(
        "test.kafka-gobblin-hdfs-test.kafka.servers.config", "server1:6667" );

    public static final String KAFKA_SERVERS_TOPIC = System.getProperty(
        "test.kafka-gobblin-hdfs-test.kafka.servers.config.topic.test", "kafka-gobblin-hdfs-test" );


    private static Properties properties = new Properties();

    static {
        try (InputStream in =
                AppConfiguration.class.getResourceAsStream("/kafka-gobblin-hdfs-test.pull")) //$NON-NLS-1$
        {
            properties.load(in);
        }
        catch ( IOException e )
        {
            throw new RuntimeException(e);
        }

    }

    public static String getGobblinProperty(String key)
    {
        return properties.getProperty( key );
    }
}
