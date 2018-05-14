package meteo.geo.stations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.google.common.io.Files;

import meteo.icing.era.Conf;
import meteo.icing.era.DataStamp;

public class StationUtils {

	
	private static String SOUNDING_ROOT_DIR = "H:/icing/Dropbox/icing/archive/sounding";
	
	public static void main( String ... args ) throws Exception
	{
		Conf conf = Conf.ERA_INTERIM;
		List <SurfaceStationInfo> stations = loadStations();
		 
		List <SurfaceStationInfo> aoistations = filterByAOI(stations, conf.minlat, conf.minlon, conf.maxlat, conf.maxlon);
		
		Set <String> adaptiveStations = new HashSet <> ();
		
		DateTime startTime = new DateTime(2012, 6, 26, 00, 00);
		
		DateTime datetime = new DateTime(startTime);
		
		int lastMonth = 0;
		boolean adapting = true;
		while( datetime.getYear() < 2017)
		{
			if( datetime.getMonthOfYear() != lastMonth)
			{
				lastMonth = datetime.getMonthOfYear();
				adaptiveStations.clear();
				for(SurfaceStationInfo ssi : aoistations)
					adaptiveStations.add( ssi.getSynop() );
				adapting = true;
			}
			else
				adapting = false;
		
			int fidx = 0;
			for( int sidx = 0; sidx < aoistations.size(); sidx ++ )
			{
				SurfaceStationInfo ssi = aoistations.get(sidx);
				if(! adaptiveStations.contains( ssi.getSynop() ))
					continue;
			
				String sondeURL = createSondeURL(
					datetime.getYear(),
					datetime.getMonthOfYear(),
					datetime.getDayOfMonth(),
					datetime.getHourOfDay(),
					ssi.getSynop());

//				System.out.println(sondeURL);
				Document doc;
				try {
					doc = Jsoup.connect(sondeURL).get();
				}
				catch(IOException e ) 
				{  
					e.printStackTrace();
					sidx --;
					continue;
				}
				
				String tableDataStr, metaDataStr;
				try {
					Elements elems = doc.getElementsByTag("pre");
					
					tableDataStr = elems.get(0).html();
					metaDataStr = elems.get(1).html();
				}
				catch(IndexOutOfBoundsException e) 
				{ 
					if(adapting)
						adaptiveStations.remove( ssi.getSynop() );
					continue;
				}
	
//					System.out.println(tableDataStr);
//					System.out.println(metaDataStr);
				try {
					
					SondeData sondeData = parseSondeData( tableDataStr, metaDataStr );
				}
				catch(Exception x) // parse failure, retry
				{
					x.printStackTrace();
					sidx --;
					continue;
					
				}
				long fixedTimestamp = datetime.getMillis();
				
				String fixedMonthStr = DataStamp.toMonthStr(fixedTimestamp);
				String fixedDayStr = DataStamp.toDayStr(fixedTimestamp);
				String fixedTimeStr = DataStamp.toTimeStr(fixedTimestamp);
				String fixedDateStr = DataStamp.toDateStr(fixedTimestamp);

				String datetimeStr =  fixedDateStr + "_" + fixedTimeStr + "Z";
				String path  = SOUNDING_ROOT_DIR + "/" + fixedMonthStr + "/" + fixedDayStr;

				new File(path).mkdirs();

				String filename = path + "/" + datetimeStr + "_" + ssi.getSynop() + "_sounding.txt";
				File file = new File( filename );
				
				
				String data = tableDataStr + "\r\n" + "===\r\n"
						+ metaDataStr;
				
				Files.write(data.getBytes(), file);
				
				fidx ++;
				System.out.println( "(" + fidx + " of " + adaptiveStations.size() + ") " +
						"Downloaded sounding data for " + datetime + ", station " + ssi.getSynop() + " to " + file);
				
			}
			datetime = datetime.plusHours(12);	
		}
				
	}
	
	private static SondeData parseSondeData(String tableDataStr, String metaDataStr) 
	{
		List <SondeSampleData> samples = new ArrayList <> ();
		
		String [] lines = tableDataStr.split("\n");
		
		String titleLine = lines[1];
		String [] titleParts = titleLine.trim().split("\\s+");
		
		int [] offsets = new int [titleParts.length+1];
		offsets[0] = 0;
		for(int idx = 0; idx < titleParts.length; idx ++)
		{
			String columnName = titleParts[idx];
			int offset = titleLine.indexOf(columnName) + columnName.length();
			offsets[idx+1] = offset;
		}
				
		
		for(int lidx = 4; lidx < lines.length; lidx ++)
		{
			String line = lines[lidx];
			
			float pressure = parseValue(line, offsets, 0, 1);
			float height = parseValue(line, offsets, 1, 2);
			float temperature = parseValue(line, offsets, 2, 3);
			float dewpointTemperature = parseValue(line, offsets, 3, 4);
			float relativeHumidity = parseValue(line, offsets, 4, 5);
			float mixingRatio = parseValue(line, offsets, 5, 6);
			float windDirection = parseValue(line, offsets, 6, 7);
			float windSpeed = parseValue(line, offsets, 7, 8);
			float thetaA = parseValue(line, offsets, 8, 9);
			float thetaE = parseValue(line, offsets, 9, 10);
			float thetaV = parseValue(line, offsets, 10, 11);		


			SondeSampleData ssd = new SondeSampleData(pressure, height, temperature, dewpointTemperature, relativeHumidity, mixingRatio, windDirection, windSpeed, thetaA, thetaE, thetaV);
			
			samples.add( ssd );
		}
		
		return new SondeData( samples );
	}

	private static float parseValue(String line, int[] offsets, int i, int j) 
	{
		if(line.length() < offsets[j])
			return Float.NaN;
		String valueStr = line.substring(offsets[i], offsets[j]);
		if( valueStr.trim().isEmpty())
			return Float.NaN;
		float value = Float.parseFloat( valueStr );
		return value;
	}

	public static List <SurfaceStationInfo> loadStations()
	{
		
		List <SurfaceStationInfo> stations = new ArrayList <> ();
		
		try ( FileInputStream fstream = new FileInputStream("etc/stations.txt") )
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
	
			String strLine;
	
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   
			{
				if(strLine.length() != 83)
					continue;
			  // Print the content on the console
				
				String stateCode = strLine.substring(0, 2).trim();
				String name = strLine.substring(3, 19).trim();
				String icao = strLine.substring(20, 25).trim();
				String iata = strLine.substring(26, 30).trim();
				String synop = strLine.substring(32, 37).trim();
				String latStr = strLine.substring(39, 45).trim();
				float lat = parseCoord( latStr );
				String lonStr = strLine.substring(47, 54).trim();
				float lon = parseCoord( lonStr );
				String elevationStr = strLine.substring(55, 59).trim();
				float elevation = Float.parseFloat( elevationStr );
				String priorityStr = strLine.substring(79, 80);
				int priority = Integer.parseInt(priorityStr);
				String countryCode = strLine.substring(81, 83).trim();
				
				SurfaceStationInfo data = new SurfaceStationInfo(stateCode, name, icao, iata, synop, lat, lon, elevation, countryCode, priority);
				
				stations.add( data );
//				System.out.println (data.toString());
			}
		}
		catch(IOException x) { throw new RuntimeException(x); } 
		
		return stations;
	}
	
	public static List <SurfaceStationInfo> filterByAOI( List <SurfaceStationInfo> stations, 
			double minLat, double minLon, double maxLat, double maxLon )
	{
		List <SurfaceStationInfo> output = new ArrayList <> ();
		for(SurfaceStationInfo ssi : stations)
		{
			if( ssi.getLat() <= maxLat && ssi.getLat() >= minLat 
			&&  ssi.getLon() <= maxLon && ssi.getLon() >= minLon )
			{
				output.add( ssi );
			}
		}
		return output;
	}
	
	public static float parseCoord( String coordStr )
	{
		float sign = 0;
		if( coordStr.endsWith("N") || coordStr.endsWith("E"))
			sign = 1;
		else if( coordStr.endsWith("S") || coordStr.endsWith("W") )
			sign = -1;
		
		String [] parts = coordStr.substring(0, coordStr.length()-1).split(" ");
		float integerPart = Float.parseFloat( parts[0] );
		float fractionPart = Float.parseFloat( parts[1] );
		float coordValue = integerPart + fractionPart/ 60;
		
		return sign * coordValue;
	}
	
	static DecimalFormat df = new DecimalFormat("00");

	public static String createSondeURL(int year, int month, int day, int hour, String stationId)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("http://weather.uwyo.edu/cgi-bin/sounding?region=naconf&TYPE=TEXT%3ALIST")
			.append("&YEAR=").append(year)
			.append("&MONTH=").append(df.format(month))
			.append("&FROM=").append(df.format(day)).append(df.format( hour ) )
			.append("&TO=").append(df.format(day)).append(df.format( hour ) )
			.append("&STNM=").append(stationId);
			
		return sb.toString();
	}
}
