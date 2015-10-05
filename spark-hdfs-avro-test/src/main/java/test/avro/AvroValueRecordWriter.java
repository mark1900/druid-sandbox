package test.avro;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.avro.Schema;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileConstants;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.mapred.AvroValue;
import org.apache.avro.mapreduce.Syncable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 *
 * @see org.apache.avro.mapreduce.AvroKeyRecordWriter
 */
public class AvroValueRecordWriter<T> extends RecordWriter<NullWritable, AvroValue<T>> implements Syncable
{
    private final DataFileWriter<T> mAvroFileWriter;

    @SuppressWarnings( "unchecked" )
    public AvroValueRecordWriter( Schema writerSchema, GenericData dataModel, CodecFactory compressionCodec,
        OutputStream outputStream, int syncInterval ) throws IOException
    {
        mAvroFileWriter = new DataFileWriter<>( dataModel.createDatumWriter( writerSchema ) );
        mAvroFileWriter.setCodec( compressionCodec );
        mAvroFileWriter.setSyncInterval( syncInterval );
        mAvroFileWriter.create( writerSchema, outputStream );
    }

    public AvroValueRecordWriter( Schema writerSchema, GenericData dataModel, CodecFactory compressionCodec,
        OutputStream outputStream ) throws IOException
    {
        this( writerSchema, dataModel, compressionCodec, outputStream,
              DataFileConstants.DEFAULT_SYNC_INTERVAL );
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings( "unused" )
    @Override
    public void write( NullWritable ignore, AvroValue<T> record ) throws IOException
    {
        mAvroFileWriter.append( record.datum() );
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings( "unused" )
    @Override
    public void close( TaskAttemptContext context ) throws IOException
    {
        mAvroFileWriter.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long sync() throws IOException
    {
        return mAvroFileWriter.sync();
    }
}