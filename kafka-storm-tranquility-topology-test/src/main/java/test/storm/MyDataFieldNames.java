package test.storm;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings( "nls" )
public final class MyDataFieldNames
{

    public static final String TIMESTAMP = "timestamp";
    public static final String VERSION = "version";
    public static final String TYPE = "type";
    public static final String LEVEL = "level";
    public static final String MESSAGE = "message";

    private MyDataFieldNames()
    {
        //
    }

    public static List<String> getNames()
    {
        List<String> columns = Arrays.asList(
            TIMESTAMP,
            VERSION,
            TYPE,
            LEVEL,
            MESSAGE );
        return columns;
    }

}
