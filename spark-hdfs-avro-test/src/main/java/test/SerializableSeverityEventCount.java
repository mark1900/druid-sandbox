package test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;

import test.avro.dto.SeverityEventCount;

/**
 *
 */
public class SerializableSeverityEventCount extends SeverityEventCount implements Serializable
{
    private static final long serialVersionUID = 1L;

    public SerializableSeverityEventCount()
    {
        // NOOP
    }

    public SerializableSeverityEventCount( SeverityEventCount avroPojo )
    {
        setValues( avroPojo );
    }

    private void setValues( SeverityEventCount avroPojo )
    {
        setLevel( avroPojo.getLevel() );
        setCount( avroPojo.getCount() );
    }

    private void writeObject( ObjectOutputStream out ) throws IOException
    {
        DatumWriter<SeverityEventCount> writer = new SpecificDatumWriter<>( SeverityEventCount.class );
        Encoder encoder = EncoderFactory.get().binaryEncoder( out, null );
        writer.write( this, encoder );
        encoder.flush();
    }

    @SuppressWarnings( "unused" )
    private void readObject( ObjectInputStream in ) throws IOException, ClassNotFoundException
    {
        DatumReader<SeverityEventCount> reader = new SpecificDatumReader<>( SeverityEventCount.class );
        Decoder decoder = DecoderFactory.get().binaryDecoder( in, null );
        setValues( reader.read( null, decoder ) );
    }

    @SuppressWarnings( "unused" )
    private void readObjectNoData() throws ObjectStreamException
    {
        // NOOP
    }

    @Override
    public int compareTo( SpecificRecord o )
    {
        try
        {
            if ( this == o )
                return 0;
            if ( o instanceof SerializableSeverityEventCount )
            {
                SerializableSeverityEventCount that = (SerializableSeverityEventCount)o;

                if ( this.getLevel() < that.getLevel() )
                    return -1;
                if ( this.getLevel() > that.getLevel() )
                    return 1;
                return 0;
            }
            throw new IllegalArgumentException( "Can only compare two SeverityEventCounts" ); //$NON-NLS-1$

        }
        catch(Exception e)
        {
            throw new RuntimeException( e );
        }
    }

    @SuppressWarnings( { "nls", "deprecation" } )
    @Override
    public String toString()
    {
        StringBuilder builder2 = new StringBuilder();
        builder2.append( "SerializableSeverityEventCount [level=" );
        builder2.append( level );
        builder2.append( ", count=" );
        builder2.append( count );
        builder2.append( "]" );
        return builder2.toString();
    }

}
