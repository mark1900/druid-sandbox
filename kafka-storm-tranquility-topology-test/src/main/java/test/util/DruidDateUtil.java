package test.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 *
 *
 * @see http
 *      ://stackoverflow.com/questions/2201925/converting-iso-8601-compliant-string-to-java-util-date
 *
 */
@SuppressWarnings( "nls" )
public final class DruidDateUtil
{
    // Example value: "2008-03-01T13:00:00+0100"
    public static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final DateFormat DATE_FORMATTER;

    static
    {

        DATE_FORMATTER = new SimpleDateFormat( ISO_8601_DATE_FORMAT );
        TimeZone tz = TimeZone.getTimeZone( "UTC" );
        DATE_FORMATTER.setTimeZone( tz );
    }

    public static final DateTimeFormatter DATETIME_FORMATTER_FOR_PARSER = ISODateTimeFormat.dateTimeParser()
        .withOffsetParsed().withZone( DateTimeZone.UTC );
    public static final DateTimeFormatter DATETIME_FORMATTER_FOR_PRINTER = ISODateTimeFormat.dateTimeNoMillis()
        .withZone( DateTimeZone.UTC );

    private DruidDateUtil()
    {
        //
    }

    public static Date toDate( String date ) throws ParseException
    {
        return DATE_FORMATTER.parse( date.replace( "Z", "+0000" ) );
    }

    public static DateTime toDateTime( String date )
    {
        return DATETIME_FORMATTER_FOR_PARSER.parseDateTime( date );
    }

    public static String fromDate( Date date )
    {
        return DATE_FORMATTER.format( date ).replace( "+0000", "Z" );
    }

    /**
     * <pre>
     *
     * See also {@link DateTime#toString()}
     * <blockquote>
     * Output the date time in ISO8601 format (yyyy-MM-ddTHH:mm:ss.SSSZZ).
     * </blockquote>
     * </pre>
     *
     * @param date
     * @return
     */
    public static String fromDateTime( DateTime date )
    {
        return DATETIME_FORMATTER_FOR_PRINTER.print( date ).replace( "+0000", "Z" );
    }

}