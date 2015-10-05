package test.avro;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.mapred.AvroValue;
import org.apache.avro.mapreduce.AvroJob;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see org.apache.avro.mapreduce.AvroKeyInputFormat
 */
public class AvroValueInputFormat<T> extends FileInputFormat<NullWritable, AvroValue<T>>
{

    private static final Logger LOGGER = LoggerFactory.getLogger( AvroValueInputFormat.class );

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings( "unused" )
    @Override
    public RecordReader<NullWritable, AvroValue<T>> createRecordReader( InputSplit split,
        TaskAttemptContext context ) throws IOException, InterruptedException
    {

        Schema readerSchema = AvroJob.getInputValueSchema( context.getConfiguration() );
        if ( null == readerSchema )
        {
            LOGGER.warn( "Reader schema was not set. Use AvroJob.setInputValueSchema() if desired." ); //$NON-NLS-1$
            LOGGER.info( "Using a reader schema equal to the writer schema." ); //$NON-NLS-1$
        }

        LOGGER.info( "## Avro Reader Schema:  " + String.valueOf( readerSchema ) ); //$NON-NLS-1$

        return new AvroValueRecordReader<>( readerSchema );
    }
}