package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.mapred.AvroValue;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Tuple2;
import test.avro.AvroValueInputFormat;
import test.avro.AvroValueOutputFormat;
import test.avro.dto.Event;
import test.avro.dto.SeverityEventCount;

public class SparkApp
{

    private static final Logger LOGGER = LoggerFactory.getLogger( SparkApp.class );

    public static void main( String[] args ) throws Exception
    {
        try
        {
            LOGGER.info( "## SparkApp Logger: " + Arrays.toString( args ) ); //$NON-NLS-1$

            SparkConf sparkConf = null;

            {
                if ( AppConfiguration.RUN_SPARK_LOCALLY )
                {
                    sparkConf = new SparkConf().setAppName( "Simple Application" ).setMaster( "local[2]" ); //$NON-NLS-1$ //$NON-NLS-2$
                }
                else
                {
                    // For example:
                    //
                    // Apache Ambari Deployment
                    // /usr/hdp/current/spark-client/bin/spark-submit --class test.SparkApp --master  yarn-cluster --num-executors 2 --driver-memory 512m --executor-memory 512m --executor-cores 1 ~/tmp/spark-hdfs-avro-test-0.0.1.jar
                    //
                    sparkConf = new SparkConf().setAppName( "Simple Application" ); //$NON-NLS-1$
                }
            }

            // http://stackoverflow.com/questions/27033823/how-to-overwrite-the-output-directory-in-spark
            sparkConf.set( "spark.hadoop.validateOutputSpecs", "false" ); //$NON-NLS-1$ //$NON-NLS-2$

            try (JavaSparkContext sc = new JavaSparkContext( sparkConf ))
            {
                execute( sc );
            }
        }
        catch ( Exception e )
        {
            LOGGER.error( "## Unexpected Exception", e ); //$NON-NLS-1$
            System.err.println( "## Unexpected Exception" ); //$NON-NLS-1$
            e.printStackTrace();
            throw e;
        }
    }

    public static void execute( JavaSparkContext sc ) throws Exception
    {

        LOGGER.info( "## Executing... " ); //$NON-NLS-1$

        LOGGER.info( "## Input File:  " + AppConfiguration.HADOOP_BASE_URI + AppConfiguration.HADOOP_INPUT_FILE_PATH ); //$NON-NLS-1$

        Configuration hadoopConf = new Configuration();

        hadoopConf.set("fs.defaultFS", AppConfiguration.HADOOP_BASE_URI); //$NON-NLS-1$

        hadoopConf.set( "avro.schema.input.key", Schema.create( org.apache.avro.Schema.Type.NULL ).toString() ); //$NON-NLS-1$
        hadoopConf.set( "avro.schema.input.value", Event.SCHEMA$.toString() ); //$NON-NLS-1$
        hadoopConf.set( "avro.schema.output.key", Schema.create( org.apache.avro.Schema.Type.NULL ).toString() ); //$NON-NLS-1$
        hadoopConf.set( "avro.schema.output.value", SeverityEventCount.SCHEMA$.toString() ); //$NON-NLS-1$

        @SuppressWarnings( "unchecked" )
        JavaPairRDD<NullWritable, AvroValue<Event>> avroRDD = sc.newAPIHadoopFile(
            AppConfiguration.HADOOP_INPUT_FILE_PATH, AvroValueInputFormat.class, NullWritable.class, AvroValue.class, hadoopConf );

        LOGGER.info( "## JavaPairRDD - avroRDD: " + avroRDD ); //$NON-NLS-1$

        JavaPairRDD<String, SerializableEvent> avroParsedData =
            avroRDD.mapToPair( new PairFunction<Tuple2<NullWritable, AvroValue<Event>>, String, SerializableEvent>()
        {
            private static final long serialVersionUID = 1L;

            @Override
            public Tuple2<String, SerializableEvent> call( Tuple2<NullWritable, AvroValue<Event>> t )
                throws Exception
            {
                Event event = t._2().datum();
                SerializableEvent serializableEvent = new SerializableEvent( event );

                return new Tuple2<>( String.valueOf( serializableEvent.getLevel() ),
                    serializableEvent );
            }
        } );

        JavaPairRDD<String, Iterable<SerializableEvent>> avroGroupedByKeyData = avroParsedData.groupByKey();

        JavaPairRDD<String, SerializableSeverityEventCount> avroMappedValueData =
            avroGroupedByKeyData.mapValues( new Function<Iterable<SerializableEvent>, SerializableSeverityEventCount>()
        {

            private static final long serialVersionUID = 1L;

            @Override
            public SerializableSeverityEventCount call( Iterable<SerializableEvent> events )
                throws Exception
            {
                List<SerializableEvent> sessionizedEvents = createList( events );
                if ( sessionizedEvents.size() > 0 )
                {
                    Collections.sort( sessionizedEvents );
                    SerializableEvent first = sessionizedEvents.get( 0 );
                    int count = sessionizedEvents.size();

                    return new SerializableSeverityEventCount( new SeverityEventCount( first
                        .getLevel(), count ) );
                }

                return new SerializableSeverityEventCount( new SeverityEventCount( 0, 0 ) );
            }

        } );

        avroMappedValueData.foreach( new VoidFunction<Tuple2<String, SerializableSeverityEventCount>>()
        {
            private static final long serialVersionUID = 1L;

            @Override
            public void call( Tuple2<String, SerializableSeverityEventCount> t ) throws Exception
            {

                LOGGER.info( "## JavaPairRDD - avroMappedValueData - Tuple2 key: " + String.valueOf( t._1() ) ); //$NON-NLS-1$
                LOGGER.info( "## JavaPairRDD - avroMappedValueData - Tuple2 value: " + String.valueOf( t._2() ) ); //$NON-NLS-1$

            }
        } );

        JavaPairRDD<NullWritable, AvroValue<SerializableSeverityEventCount>> outputPairs =
            avroMappedValueData.mapToPair( new PairFunction<Tuple2<String, SerializableSeverityEventCount>, NullWritable, AvroValue<SerializableSeverityEventCount>>()
        {

            private static final long serialVersionUID = 1L;

            @Override
            public Tuple2<NullWritable, AvroValue<SerializableSeverityEventCount>> call(
                Tuple2<String, SerializableSeverityEventCount> t ) throws Exception
            {
                return new Tuple2<>( NullWritable.get(), new AvroValue<>( t._2() ) );
            }

        } );

        LOGGER.info( "## Output Directory:  " + AppConfiguration.HADOOP_BASE_URI + AppConfiguration.HADOOP_OUTPUT_DIRECTORY_PATH ); //$NON-NLS-1$

        try (FileSystem fs = FileSystem.get( hadoopConf ))
        {
            // delete file, true for recursive
            fs.delete( new Path( AppConfiguration.HADOOP_OUTPUT_DIRECTORY_PATH ), true );
        }

        outputPairs.saveAsNewAPIHadoopFile(
            AppConfiguration.HADOOP_OUTPUT_DIRECTORY_PATH, NullWritable.class, AvroValue.class, AvroValueOutputFormat.class, hadoopConf );

        LOGGER.info( "## Done!" ); //$NON-NLS-1$

    }

    private static <T> List<T> createList( Iterable<T> iter )
    {
        ArrayList<T> list = new ArrayList<>();
        for ( T item : iter )
        {
            list.add( item );
        }
        return list;
    }

    @SuppressWarnings( "unused" )
    private static <T> List<T> createList( Iterator<T> iter )
    {
        List<T> copy = new ArrayList<>();
        while ( iter.hasNext() )
        {
            copy.add( iter.next() );
        }

        return copy;
    }

}