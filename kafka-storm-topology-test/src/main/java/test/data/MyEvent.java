package test.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings( "nls" )
public class MyEvent implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty( "type" )
    private String type;

    @JsonProperty( "level" )
    private Integer level;

    @JsonProperty( "message" )
    private String message;

    public MyEvent()
    {
        super();
    }

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public Integer getLevel()
    {
        return level;
    }

    public void setLevel( Integer level )
    {
        this.level = level;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage( String message )
    {
        this.message = message;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append( "MyEvent [type=" );
        builder.append( type );
        builder.append( ", level=" );
        builder.append( level );
        builder.append( ", message=" );
        builder.append( message );
        builder.append( "]" );
        return builder.toString();
    }

}
