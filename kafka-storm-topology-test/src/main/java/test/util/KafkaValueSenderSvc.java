package test.util;

import java.util.List;


public interface KafkaValueSenderSvc
{

    public abstract void send( String topic, String value );
    public abstract void send( String topic, List<String> values );

    public abstract void sendAndWait( String topic, String value, Long timeout );
    public abstract void sendAndWait( String topic, List<String> values, Long timeout );

}