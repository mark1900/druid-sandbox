package test;

import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.SeekableInput;
import org.apache.avro.io.DatumReader;
import org.apache.avro.mapred.FsInput;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import test.avro.dto.SeverityEventCount;

public class AvroOutputFileReadTest
{


    public static void main( String[] args ) throws Exception
    {
        String filePath = AppConfiguration.HADOOP_OUTPUT_DIRECTORY_PATH + "/part-r-00000.avro"; //$NON-NLS-1$
        System.out.println( "Reading file:  " + AppConfiguration.HADOOP_BASE_URI + filePath ); //$NON-NLS-1$

        Configuration hadoopConf = new Configuration();
        hadoopConf.set("fs.defaultFS", AppConfiguration.HADOOP_BASE_URI); //$NON-NLS-1$

        try ( FileSystem fs = FileSystem.get( hadoopConf ) )
        {

            Path path = new Path( filePath );

            try ( SeekableInput input = new FsInput( path, hadoopConf ) )
            {
                DatumReader<SeverityEventCount> datumReader = new SpecificDatumReader<>( SeverityEventCount.class );

                try ( DataFileReader<SeverityEventCount> dataFileReader = new DataFileReader<>( input, datumReader ) )
                {

                    SeverityEventCount severityEventCount = null;
                    while ( dataFileReader.hasNext() )
                    {
                        severityEventCount = dataFileReader.next( severityEventCount );
                        System.out.println( severityEventCount );
                    }
                }
            }
        }

        System.out.println( "Completed Successfully!" ); //$NON-NLS-1$
    }
}

