package test;

import gobblin.configuration.WorkUnitState;
import gobblin.source.extractor.Extractor;
import gobblin.source.extractor.extract.kafka.KafkaSource;

import java.io.IOException;

/**
 *
 */
public class KafkaJsonSource extends KafkaSource<String, byte[]>
{

    /**
     * {@inheritDoc}
     */
    @Override
    public Extractor<String, byte[]> getExtractor( WorkUnitState state ) throws IOException
    {
        return new KafkaJsonExtractor(state);
    }

}
