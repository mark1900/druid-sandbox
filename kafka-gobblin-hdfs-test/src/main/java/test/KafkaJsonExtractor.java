package test;

import gobblin.configuration.WorkUnitState;
import gobblin.metrics.kafka.SchemaNotFoundException;
import gobblin.source.extractor.extract.kafka.KafkaExtractor;

import java.io.IOException;

import kafka.message.MessageAndOffset;

/**
 *
 */
public class KafkaJsonExtractor extends KafkaExtractor<String, byte[]>
{

    public KafkaJsonExtractor(WorkUnitState state) {
        super(state);
      }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSchema() throws IOException
    {
        return this.topicName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected byte[] decodeRecord( MessageAndOffset messageAndOffset ) throws SchemaNotFoundException, IOException
    {
        return getBytes(messageAndOffset.message().payload());
    }


}
