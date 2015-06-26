package test.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import test.storm.AppConfiguration;


/**
 *
 *
 * @see http://kafka.apache.org/082/javadoc/index.html?org/apache/kafka/clients/producer/KafkaProducerhtml
 * @see https://github.com/CameronGregory/kafka/blob/master/TestProducer.java
 */
public class KafkaValueSenderSvcImpl implements KafkaValueSenderSvc
{
    private static final Logger LOGGER = LoggerFactory.getLogger( KafkaValueSenderSvc.class );

    private Properties producerProperties;

    /**
     * <blockquote>
     *
     * <pre>
     * Implementation Options:
     *   Option 1: KafkaProducer<String, String> paired with the StringSerializer.class
     *   Option 2: KafkaProducer<byte[], byte[]> paired with the ByteArraySerializer.class
     *
     *
     * </pre>
     *
     * </blockquote>
     *
     */
    private KafkaProducer<String, String> producer;

    public void init( final String servers )
    {
        producerProperties = new Properties( System.getProperties() );

        producerProperties.put( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers );

        producerProperties.put( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName() );
        producerProperties.put( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName() );

        producerProperties.put( ProducerConfig.ACKS_CONFIG, AppConfiguration.KAFKA_SERVERS_CONFIG_PRODUCER_ACKS );
        producerProperties.put( ProducerConfig.RETRIES_CONFIG, AppConfiguration.KAFKA_SERVERS_CONFIG_PRODUCER_RETRIES );
        producerProperties.put( ProducerConfig.RETRY_BACKOFF_MS_CONFIG, AppConfiguration.KAFKA_SERVERS_CONFIG_PRODUCER_RETRY_BACKOFF );

        // producerProperties.put( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName() );
        // producerProperties.put( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName() );

        producer = new KafkaProducer<>( producerProperties );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send( String topic, String value )
    {
        if ( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug( "Sending Kafka Request [topic={},value={}]", topic, value ); //$NON-NLS-1$
        }

        ProducerRecord<String, String> producerRecord = new ProducerRecord<>( topic, value );
        producer.send( producerRecord, new ProducerRecordLoggingCallback( producerRecord ) );

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send( final String topic, final List<String> values )
    {

        for ( final String value : values )
        {
            if ( LOGGER.isDebugEnabled() )
            {
                LOGGER.debug( "Sending Kafka Request [topic={},value={}]", topic, value ); //$NON-NLS-1$
            }

            ProducerRecord<String, String> producerRecord = new ProducerRecord<>( topic, value );
            producer.send( producerRecord, new ProducerRecordLoggingCallback( producerRecord ) );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendAndWait( String topic, String value, Long timeout )
    {
        if ( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug( "Sending Kafka Request [topic={},value={},timeout={}]", topic, value, timeout ); //$NON-NLS-1$
        }

        ProducerRecord<String, String> producerRecord = new ProducerRecord<>( topic, value );
        Future<RecordMetadata> future = producer.send( producerRecord, new ProducerRecordLoggingCallback( producerRecord ) );


        // Try to wait for records to be processed
        try
        {
            if ( timeout == null || timeout < 1 )
            {
                future.get();
            }
            else
            {
               future.get( timeout, TimeUnit.MILLISECONDS );
            }
        } catch (Exception e)
        {
            LOGGER.error( "Unexpected exception!", e ); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendAndWait( String topic, List<String> values, Long timeout )
    {

        List<Future<RecordMetadata>> futures = new ArrayList<>(values.size());

        for ( final String value : values )
        {
            if ( LOGGER.isDebugEnabled() )
            {
                LOGGER.debug( "Sending Kafka Request [topic={},value={},timeout={}]", topic, value, timeout ); //$NON-NLS-1$
            }

            ProducerRecord<String, String> producerRecord = new ProducerRecord<>( topic, value );

            Future<RecordMetadata> future = producer.send( producerRecord, new ProducerRecordLoggingCallback( producerRecord ) );
            futures.add( future );

            if ( LOGGER.isDebugEnabled() )
            {
                LOGGER.debug( "Sent Kafka Request" ); //$NON-NLS-1$
            }
        }


        // Try to wait for records to be processed
        List<RecordMetadata> records = new ArrayList<>(futures.size());

        for ( Future<RecordMetadata> future : futures )
        {
            try
            {
                if ( timeout == null || timeout < 1 )
                {
                    records.add( future.get() );
                }
                else
                {
                    records.add( future.get( timeout, TimeUnit.MILLISECONDS ) );
                }
            } catch (Exception e)
            {
                LOGGER.error( "Unexpected exception!", e ); //$NON-NLS-1$
            }
        }


    }

    public Properties getProducerProperties()
    {
        return producerProperties;
    }

    public KafkaProducer<String, String> getProducer()
    {
        return producer;
    }

    public void shutdown()
    {
        LOGGER.info( "Shutting down Kafka Producer ..." ); //$NON-NLS-1$
        producer.close();
        LOGGER.info( "Kafka Producer Shutdown." ); //$NON-NLS-1$
    }

    public void addShutdownHook()
    {
        Runtime.getRuntime().addShutdownHook( new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                LOGGER.info( "Shutting down Kafka Producer using shutdown hook ..." ); //$NON-NLS-1$
                producer.close();
                LOGGER.info( "Kafka Producer Shutdown." ); //$NON-NLS-1$
            }
        } ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "KafkaProducerSvc [" //$NON-NLS-1$
                + "producer=" + producer //$NON-NLS-1$
                + ", producerProperties=" + producerProperties //$NON-NLS-1$
                + "]"; //$NON-NLS-1$
    }



}