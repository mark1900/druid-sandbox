package test.avro;

import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.mapred.AvroValue;
import org.apache.avro.mapreduce.AvroRecordReaderBase;
import org.apache.hadoop.io.NullWritable;

/**
 *
 *
 * @see org.apache.avro.mapreduce.AvroKeyRecordReader
 */
public class AvroValueRecordReader<T> extends AvroRecordReaderBase<NullWritable, AvroValue<T>, T>
{
    private final AvroValue<T> mCurrentRecord;

    public AvroValueRecordReader( Schema readerSchema )
    {
        super( readerSchema );
        mCurrentRecord = new AvroValue<>( null );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException
    {
        boolean hasNext = super.nextKeyValue();
        mCurrentRecord.datum( getCurrentRecord() );
        return hasNext;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings( "unused" )
    @Override
    public NullWritable getCurrentKey() throws IOException, InterruptedException
    {
        return NullWritable.get();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings( "unused" )
    @Override
    public AvroValue<T> getCurrentValue() throws IOException, InterruptedException
    {
        return mCurrentRecord;
    }
}