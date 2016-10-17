/*
 *
 * (C) Copyright 2012-2013 ECMWF.
 *
 * This software is licensed under the terms of the Apache Licence Version 2.0
 * which can be obtained at http://www.apache.org/licenses/LICENSE-2.0.
 * In applying this licence, ECMWF does not waive the privileges and immunities
 * granted to it by virtue of its status as an intergovernmental organisation nor
 * does it submit to any jurisdiction.
 *
 */

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.ecmwf.APIError;
import org.ecmwf.DataServer;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;

public class ECMWFDownloader {

	static Map <String, Map <String, String>> ERA_INTERIM_REQUESTS = new HashMap <> ();

	public static String FORMAT_GRIB = "grib";
	public static String FORMAT_NETCDF = "netcdf";
	private static String FORMAT = FORMAT_NETCDF;


	public static String LEVELS_ALL = "1/2/3/5/7/10/20/30/50/70/100/125/150/175/200/225/250/300/350/400/450/500/550/600/650/700/750/775/800/825/850/875/900/925/950/975/1000";
	public static String LEVELS_ICING = "100/150/200/250/300/350/400/450/500/550/600/650/700/750/800/850/900/950/1000";

//	public static String PARAM_TEMP = "130.128";
//	public static String PARAM_VERTICAL_VELOCITY = "135.128";
//	public static String PARAM_RELATIVE_HUMIDITY = "157.128";

	public static Map <String, String> ERA_INTERIM_PARAMS = new HashMap <String, String> ();
	static {
		ERA_INTERIM_PARAMS.put("T", "130.128");
		ERA_INTERIM_PARAMS.put("W", "135.128");
		ERA_INTERIM_PARAMS.put("RH", "157.128");
	}



//	public static String [] ERA_INTERIM_PARAMS = new String [] { PARAM_TEMP, PARAM_VERTICAL_VELOCITY, PARAM_RELATIVE_HUMIDITY };
	static {
		Map <String, String> isobaric = new HashMap <> ();
		isobaric.put("step", "0");
		isobaric.put("levtype", "pl");
		isobaric.put("levelist", LEVELS_ICING );
//		isobaric.put("param","60.128/129.128/130.128/131.128/132.128/133.128/135.128/138.128/155.128/157.128/203.128/246.128/247.128/248.128");
		isobaric.put("type","an");

		ERA_INTERIM_REQUESTS.put("isobaric", isobaric);

/*		Map <String, String> sfc0Step = new HashMap <> ();
		sfc0Step.put("step", "0");
		sfc0Step.put("levtype", "sfc");
		sfc0Step.put("param","55.162/56.162/57.162/79.162/80.162/134.128/136.128/137.128/151.128/164.128/186.128/187.128/188.128");
		sfc0Step.put("type","an");

		ERA_INTERIM_REQUESTS.put("sfc0Step", sfc0Step);

		Map <String, String> sfcOther = new HashMap <> ();
		sfc0Step.put("step", "3/6/9/12");
		sfc0Step.put("levtype", "sfc");
		sfc0Step.put("param","59.128/78.128/79.128/134.128/136.128/137.128/142.128/143.128/151.128/159.128/164.128/186.128/187.128/188.128/228.128");
		sfc0Step.put("type","fc");

		ERA_INTERIM_REQUESTS.put("sfcOther", sfcOther);*/
	}

	static Map <String, String> ERA_INTERIM_HEADER = new HashMap <> ();
	static {
		ERA_INTERIM_HEADER.put("class","ei");
		ERA_INTERIM_HEADER.put("dataset", "interim");
		ERA_INTERIM_HEADER.put("expver","1");
		ERA_INTERIM_HEADER.put("stream","oper");
		ERA_INTERIM_HEADER.put("grid", "0.75/0.75");
		ERA_INTERIM_HEADER.put("format", FORMAT);
		ERA_INTERIM_HEADER.put("area", "50/-5/5/75");

	}

	static Map <String, String> CAMS_HEADER = new HashMap <> ();
	static {
		CAMS_HEADER.put("class","mc");
		CAMS_HEADER.put("dataset", "cams_nrealtime");
		CAMS_HEADER.put("expver","0001");
		CAMS_HEADER.put("stream","oper");
		CAMS_HEADER.put("format", FORMAT);
		//CAMS_HEADER.put("grid", "0.75/0.75");

	}


	static Map <String, Map <String, String>> CAMS_REQUESTS = new HashMap <> ();

	static {
		Map <String, String> analytic = new HashMap <> ();
		analytic.put("step", "3");
		analytic.put("levtype", "pl");
		analytic.put("levelist","1/2/3/5/7/10/20/30/50/70/100/150/200/250/300/400/500/700/850/925/1000");
		analytic.put("param","60.128/129.128/130.128/155.128/157.128");
		analytic.put("type","fc");

		Map <String, String> forecast = new HashMap <> ();
		forecast.put("step", "0");
		forecast.put("levtype", "pl");
		forecast.put("levelist","1/2/3/5/7/10/20/30/50/70/100/150/200/250/300/400/500/700/850/925/1000");
		forecast.put("param","129.128/130.128/157.128");
		forecast.put("type","an");

		CAMS_REQUESTS.put("isobaric", forecast);
	}
	private static class DataStamp {
		String path = "junk";
		String time;
		String date;
		public DataStamp() {}
		public DataStamp(String time, String date) { this.time = time; this.date = date; }
	}

	public static void main(String[] args) throws Exception {
		DataServer server = new DataServer();



		DateTimeZone.setDefault(DateTimeZone.UTC);
		DateTime startingTime = new DateTime( 2016, 5, 30, 0, 0 );
		DateTime endingTime = new DateTime( 2014, 1, 1, 0, 0 );


		DateTimeFormatter dayFormat = DateTimeFormat.forPattern("dd");
		DateTimeFormatter monthYearFormat = DateTimeFormat.forPattern("yyyy-MM");
		DateTimeFormatter hourFormat = DateTimeFormat.forPattern("HH");

		Queue <DataStamp> stamps = new LinkedList <> ();

		for(DateTime datetime = startingTime ;
				datetime.isAfter( endingTime); datetime = datetime.plusHours(-6) )
		{
			String dayStr = dayFormat.print(datetime);
			String monthStr = monthYearFormat.print(datetime);
			String hourStr = hourFormat.print(datetime);

			DataStamp stamp = new DataStamp();
			stamp.date = monthStr + "-" + dayStr;
			stamp.time = hourStr;
			stamp.path  = "download/era-interim/" + monthStr + "/" + dayStr;

			stamps.add( stamp );

			System.out.println(stamp.path);
		}

		Map<String, String> headers = ERA_INTERIM_HEADER;
		Map<String, Map<String, String>> paramsets = ERA_INTERIM_REQUESTS;


		stamp:while(!stamps.isEmpty())
		{
			DataStamp stamp = stamps.peek();

			boolean stampProblem = false;
			new File(stamp.path).mkdirs();

				for(String type : paramsets.keySet())
				{
					for(String paramName : ERA_INTERIM_PARAMS.keySet())
					{


						String filename = stamp.path + "/" + stamp.date + "_" + stamp.time + "Z_" + paramName +"_"+ type + ".nc";
						File file = new File( filename );


						if( file.exists() && file.length() > 0)
							continue;

						JSONObject request = new JSONObject();

						for(String headerKey : headers.keySet())
							request.put( headerKey, headers.get( headerKey ));

						request.put("param", ERA_INTERIM_PARAMS.get(paramName));
						request.put("date", stamp.date );
						request.put("time", stamp.time + ":00:00" );
						request.put("target",filename);

						Map <String, String> params = paramsets.get( type );
						for(Map.Entry <String, String> entry : params.entrySet())
						{
							request.put( entry.getKey(), entry.getValue() );
						}


						try {
							server.retrieve(request);
						}
						catch( APIError e )
						{
							e.printStackTrace();
							stampProblem = true;
						}
					}
				}
			if(! stampProblem )
				stamps.poll();
		}


	}

	/**
	 * server.retrieve({
    "class": "ei",
    "dataset": "interim",
    "date": "2016-01-01/to/2016-01-31",
    "expver": "1",
    "grid": "0.75/0.75",
    "levtype": "sfc",
    "param": "55.162/56.162/57.162/79.162/80.162/134.128/136.128/137.128/151.128/164.128/186.128/187.128/188.128",
    "step": "0",
    "stream": "oper",
    "time": "00:00:00",
    "type": "an",
    "target": "CHANGEME",
})
server.retrieve({
    "class": "ei",
    "dataset": "interim",
    "date": "2016-01-01/to/2016-01-31",
    "expver": "1",
    "grid": "0.75/0.75",
    "levtype": "sfc",
    "param": "59.128/78.128/79.128/134.128/136.128/137.128/142.128/143.128/151.128/159.128/164.128/186.128/187.128/188.128/228.128",
    "step": "3/6/9/12",
    "stream": "oper",
    "time": "00:00:00",
    "type": "fc",
    "target": "CHANGEME",
})
	 */
}
