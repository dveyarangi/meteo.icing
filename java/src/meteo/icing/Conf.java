package meteo.icing;

import java.util.HashMap;
import java.util.Map;

public class Conf
{
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
		ERA_INTERIM_PARAMS.put("CC", "248.128"); // cloud cover
		ERA_INTERIM_PARAMS.put("DIV", "155.128"); // divergence
		ERA_INTERIM_PARAMS.put("GP", "129.128"); // geopotential
		ERA_INTERIM_PARAMS.put("PV", "60.128"); // potential vorticity
		ERA_INTERIM_PARAMS.put("CIWC", "247.128"); // specific ice water content
		ERA_INTERIM_PARAMS.put("CLWC", "246.128"); // specific liquid water content
		ERA_INTERIM_PARAMS.put("U", "131.128"); // u wind comp
		ERA_INTERIM_PARAMS.put("V", "132.128"); // v wind comp
		ERA_INTERIM_PARAMS.put("V0", "138.128"); // Relative vorticity

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


	public static final Conf ERA_INTERIM = new Conf("H:/icing/era-interim");

	static {
		ERA_INTERIM.headers = ERA_INTERIM_HEADER;
		ERA_INTERIM.paramSets = ERA_INTERIM_REQUESTS;
		ERA_INTERIM.params = ERA_INTERIM_PARAMS;
	}

	public String rootDir;

	public Map<String, String> headers;
	public Map<String, Map<String, String>> paramSets;
	public Map<String, String> params;

	public Conf(String rootDir)
	{
		this.rootDir = rootDir;
	}

	public Map<String, String> getHeaders() { return headers; }

	public Map<String, Map<String, String>> getParamSets() { return paramSets; }

	public Map <String, String> getParams()	{ return params; }



}
