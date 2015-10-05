package test.avro;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.avro.Schema;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.generic.GenericData;
import org.apache.avro.hadoop.io.AvroSerialization;
import org.apache.avro.mapred.AvroValue;
import org.apache.avro.mapreduce.AvroJob;
import org.apache.avro.mapreduce.AvroOutputFormatBase;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 * @see org.apache.avro.mapreduce.AvroKeyOutputFormat
 */
public class AvroValueOutputFormat<T> extends AvroOutputFormatBase<NullWritable, AvroValue<T>>
{

    @SuppressWarnings( "rawtypes" )
    private final ArvoValueRecordWriterFactory mRecordWriterFactory;

    @SuppressWarnings( "rawtypes" )
    public AvroValueOutputFormat()
    {
        this( new ArvoValueRecordWriterFactory() );
    }

    @SuppressWarnings( "rawtypes" )
    public AvroValueOutputFormat( ArvoValueRecordWriterFactory recordWriterFactory )
    {
        mRecordWriterFactory = recordWriterFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings( "unchecked" )
    public RecordWriter<NullWritable, AvroValue<T>> getRecordWriter( TaskAttemptContext context )
        throws IOException
    {
        Configuration conf = context.getConfiguration();
        // Get the writer schema.
        Schema writerSchema = AvroJob.getOutputValueSchema( conf );
        boolean isMapOnly = context.getNumReduceTasks() == 0;
        if ( isMapOnly )
        {
            Schema mapOutputSchema = AvroJob.getMapOutputValueSchema( conf );
            if ( mapOutputSchema != null )
            {
                writerSchema = mapOutputSchema;
            }
        }
        if ( null == writerSchema )
        {
            throw new IOException(
                "AvroValueOutputFormat requires an output schema. Use AvroJob.setOutputValueSchema()." ); //$NON-NLS-1$
        }

        GenericData dataModel = AvroSerialization.createDataModel( conf );

        return mRecordWriterFactory.create( writerSchema, dataModel, getCompressionCodec( context ),
                                            getAvroFileOutputStream( context ), getSyncInterval( context ) );
    }

    public static class ArvoValueRecordWriterFactory<T>
    {

        public ArvoValueRecordWriterFactory()
        {

        }

        public RecordWriter<NullWritable, AvroValue<T>> create( Schema writerSchema, GenericData dataModel,
            CodecFactory compressionCodec, OutputStream outputStream, int syncInterval ) throws IOException
        {
            return new AvroValueRecordWriter<>( writerSchema, dataModel, compressionCodec, outputStream,
                syncInterval );
        }
    }
}
