package test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Date;

import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;

import test.avro.dto.Event;
import test.druid.DruidDateUtil;


public class SerializableEvent extends Event implements Serializable
{
    private static final long serialVersionUID = 1L;

    public SerializableEvent()
    {
        // NOOP
    }

    public SerializableEvent( Event avroPojo )
    {
        setValues( avroPojo );
    }

    private void setValues( Event avroPojo )
    {
        setTimestamp( avroPojo.getTimestamp() );
        setType( avroPojo.getType() );
        setLevel( avroPojo.getLevel() );
        setMessage( avroPojo.getMessage() );
    }

    private void writeObject( ObjectOutputStream out ) throws IOException
    {
        DatumWriter<Event> writer = new SpecificDatumWriter<>( Event.class );
        Encoder encoder = EncoderFactory.get().binaryEncoder( out, null );
        writer.write( this, encoder );
        encoder.flush();
    }

    @SuppressWarnings( "unused" )
    private void readObject( ObjectInputStream in ) throws IOException, ClassNotFoundException
    {
        DatumReader<Event> reader = new SpecificDatumReader<>( Event.class );
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
            if ( o instanceof SerializableEvent )
            {
                SerializableEvent that = (SerializableEvent)o;
                Date thisDate = DruidDateUtil.toDate( String.valueOf( this.getTimestamp() ) );
                Date thatDate = DruidDateUtil.toDate( String.valueOf( that.getTimestamp() ) );

                if ( thisDate.before( thatDate ) )
                    return -1;
                if ( thisDate.after( thatDate ) )
                    return 1;
                return 0;
            }
            throw new IllegalArgumentException( "Can only compare two Events" ); //$NON-NLS-1$

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
        builder2.append( "SerializableEvent [timestamp=" );
        builder2.append( timestamp );
        builder2.append( ", type=" );
        builder2.append( type );
        builder2.append( ", level=" );
        builder2.append( level );
        builder2.append( ", message=" );
        builder2.append( message );
        builder2.append( "]" );
        return builder2.toString();
    }

}