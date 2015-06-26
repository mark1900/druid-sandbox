package test.util;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerRecordLoggingCallback implements Callback
{
    private static final Logger LOGGER = LoggerFactory.getLogger( ProducerRecordLoggingCallback.class );

    private ProducerRecord<String, String> producerRecord;

    public ProducerRecordLoggingCallback( ProducerRecord<String, String> producerRecord )
    {
        this.producerRecord = producerRecord;
    }

    @Override
    public void onCompletion( RecordMetadata metadata, Exception exception )
    {

        if ( metadata != null )
        {
            if ( LOGGER.isDebugEnabled() )
            {
                LOGGER.debug( "Kafka record offset: " + metadata.offset() ); //$NON-NLS-1$
            }
        }

        if ( exception != null )
        {
            LOGGER.error( "Failed to send value to Kafka:  " + String.valueOf( producerRecord.value() ), //$NON-NLS-1$
                          exception );
        }

    }
}