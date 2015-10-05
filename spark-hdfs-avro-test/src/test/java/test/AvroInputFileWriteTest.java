package test;

import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import test.avro.dto.Event;

public class AvroInputFileWriteTest
{


    public static void main( String[] args ) throws Exception
    {

        String filePath = AppConfiguration.HADOOP_INPUT_FILE_PATH;
        System.out.println( "Writing file:  " + AppConfiguration.HADOOP_BASE_URI + filePath ); //$NON-NLS-1$

        Configuration hadoopConf = new Configuration();
        hadoopConf.set("fs.defaultFS", AppConfiguration.HADOOP_BASE_URI); //$NON-NLS-1$

        try ( FileSystem fs = FileSystem.get( hadoopConf ) )
        {

            Path path = new Path( filePath );

            try ( FSDataOutputStream out = fs.create( path, true ) )
            {


                DatumWriter<Event> datumWriter = new GenericDatumWriter<>(Event.SCHEMA$);
                try ( DataFileWriter<Event> dataFileWriter = new DataFileWriter<>(datumWriter) )
                {
                    dataFileWriter.create(Event.SCHEMA$, out);

                    dataFileWriter.append( new Event( "2015-08-21T17:08:00-0400","SYSTEM", 5, "RAM usage above 80 per cent utilization" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    dataFileWriter.append( new Event( "2015-08-21T17:08:01-0400","SYSTEM", 5, "RAM usage above 80 per cent utilization" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    dataFileWriter.append( new Event( "2015-08-21T17:08:02-0400","SYSTEM", 5, "RAM usage above 80 per cent utilization" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    dataFileWriter.append( new Event( "2015-08-21T17:08:03-0400","SYSTEM", 6, "RAM usage above 85 per cent utilization" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    dataFileWriter.append( new Event( "2015-08-21T17:08:04-0400","SYSTEM", 6, "RAM usage above 85 per cent utilization" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    dataFileWriter.append( new Event( "2015-08-21T17:08:05-0400","SYSTEM", 7, "RAM usage above 90 per cent utilization" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    dataFileWriter.append( new Event( "2015-08-21T17:08:06-0400","SYSTEM", 9, "RAM usage above 95 per cent utilization" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    dataFileWriter.append( new Event( "2015-08-21T17:08:07-0400","SYSTEM", 9, "RAM usage above 95 per cent utilization" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

                    dataFileWriter.flush();
                }
            }
        }

        System.out.println( "Completed Successfully!" ); //$NON-NLS-1$
    }

}
