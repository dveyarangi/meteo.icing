package meteo.icing.era;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DataStamp
{
	DateTime timestamp;

	public DataStamp(DateTime timestamp) { this.timestamp = timestamp; }

	public static DateTimeFormatter dayFormat = DateTimeFormat.forPattern("dd");
	public static DateTimeFormatter monthYearFormat = DateTimeFormat.forPattern("yyyy-MM");
	public static DateTimeFormatter hourFormat = DateTimeFormat.forPattern("HH");


	public static String toTimeStr( long timestamp )
	{
		DateTime datetime = new DateTime(timestamp);
		String hourStr = hourFormat.print(datetime);
		return hourStr;
	}
	public static String toDateStr( long timestamp )
	{
		DateTime datetime = new DateTime(timestamp);

		String dayStr = dayFormat.print(datetime);
		String monthStr = monthYearFormat.print(datetime);
		String date = monthStr + "-" + dayStr;

		return date;
	}

	public static String toDayStr( long timestamp )
	{
		DateTime datetime = new DateTime(timestamp);

		String dayStr = dayFormat.print(datetime);
		String date = dayStr;

		return date;
	}
	public static String toMonthStr( long timestamp )
	{
		DateTime datetime = new DateTime(timestamp);

		String monthStr = monthYearFormat.print(datetime);
		String date = monthStr;

		return date;
	}

}
