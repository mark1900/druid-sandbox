package test.task;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.StreamTask;
import org.apache.samza.task.TaskCoordinator;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProcessingStreamTask implements StreamTask
{

    public static final String PHASE_02_SYSTEM_NAME = "kafka"; //$NON-NLS-1$
    public static final String PHASE_02_STREAM_NAME = "kafka_samza_test_phase_02"; //$NON-NLS-1$

    /**
     *  Hadoop Container Logs
     *
     *  org.slf4j.Logger:  samza-application-master.log
     *
     *  System.out.println("") : stdout
     *  System.err.println("") : stderr
     *  Garbarge Collector - : gc.log.0.current
     */
    private static final Logger LOGGER = LoggerFactory.getLogger( ProcessingStreamTask.class );

    private static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ"; //$NON-NLS-1$
    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat( ISO_8601_DATE_FORMAT );

    private static final JsonFactory jsonFactory = new JsonFactory();

    private static final AtomicLong counter = new AtomicLong();

    static {
        LOGGER.info( "Initializing ProcessingStreamTask 04" ); //$NON-NLS-1$
    }

    public ProcessingStreamTask()
    {

    }

    @SuppressWarnings( { "unused" } )
    @Override
    public void process( IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator )
    {
        LOGGER.info("ProcessingStreamTask -> process( ... )"); //$NON-NLS-1$

        if ( envelope.getMessage() == null )
        {
            outputFailure( envelope, null, "Ignoring empty message!" ); //$NON-NLS-1$
            return;
        }

        String input = null;
        try
        {
            input = (String)envelope.getMessage();
        }
        catch ( Exception e )
        {
            outputFailure( envelope, e,
                "Unexpected incoming message type:  " + envelope.getMessage().getClass().getName() ); //$NON-NLS-1$
            return;
        }

        ObjectMapper mapper = new ObjectMapper( jsonFactory );

        Map<String, Object> outputMap = new HashMap<>();
        try {

            //convert JSON string to Map
            outputMap = mapper.readValue( input,
                new TypeReference<HashMap<String,String>>()
                {
                    //
                });

        } catch (Exception e) {
            outputFailure( envelope, e, "Failed to parse incoming message!" ); //$NON-NLS-1$
            return;
        }

        try
        {

            outputMap.put( "counter", counter.incrementAndGet() ); //$NON-NLS-1$
            outputMap.put( "now", fromDate( new Date() ) ); //$NON-NLS-1$

            String output = mapper.writeValueAsString( outputMap );
            collector.send( new OutgoingMessageEnvelope(
                new SystemStream( PHASE_02_SYSTEM_NAME, PHASE_02_STREAM_NAME ), output ) );

        }
        catch ( Exception e )
        {
            outputFailure( envelope, e, "Failed to send outgoing message.  Will rety." ); //$NON-NLS-1$
            throw new RuntimeException( e );
        }

        LOGGER.info("ProcessingStreamTask <- process( ... )"); //$NON-NLS-1$
    }

    private static void outputFailure( IncomingMessageEnvelope envelope, Exception exception, String reason )
    {
        StringBuilder output = new StringBuilder();

        if ( reason == null )
        {
            output.append( "Processing failure." ); //$NON-NLS-1$
            output.append( " Message[offset=" ); //$NON-NLS-1$
            output.append( envelope.getOffset() );
            output.append( ", contents=" ); //$NON-NLS-1$
            output.append( String.valueOf( envelope.getMessage() ) );
            output.append( "]" ); //$NON-NLS-1$

        }
        else
        {
            output.append( reason );
            output.append( "  Message[offset=" ); //$NON-NLS-1$
            output.append( envelope.getOffset() );
            output.append( ", contents=" ); //$NON-NLS-1$
            output.append( String.valueOf( envelope.getMessage() ) );
            output.append( "]" ); //$NON-NLS-1$
        }

        LOGGER.error( output.toString() );

        if ( exception != null )
        {
//            StringWriter writer = new StringWriter();
//            PrintWriter printWriter = new PrintWriter( writer );
//            exception.printStackTrace( printWriter );
//            printWriter.flush();
//            String stackTrace = writer.toString();
//
            exception.printStackTrace();
        }

    }

    private static String fromDate( Date date )
    {
        return DATE_FORMATTER.format( date ).replace( "+0000", "Z" ); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
