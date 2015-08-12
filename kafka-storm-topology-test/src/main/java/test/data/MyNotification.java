package test.data;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings( "nls" )
public class MyNotification implements Serializable
{

    private static final long serialVersionUID = 1L;

    @JsonProperty( "version" )
    private String version;

    @JsonProperty( "secret" )
    private String secret;

    @JsonProperty( "events" )
    private List<MyEvent> events;

    public MyNotification()
    {
        super();
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getSecret()
    {
        return secret;
    }

    public void setSecret( String secret )
    {
        this.secret = secret;
    }

    public List<MyEvent> getEvents()
    {
        return events;
    }

    public void setEvents( List<MyEvent> events )
    {
        this.events = events;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append( "MyNotification [version=" );
        builder.append( version );
        builder.append( ", secret=" );
        builder.append( secret );
        builder.append( ", events=" );
        builder.append( events );
        builder.append( "]" );
        return builder.toString();
    }

}