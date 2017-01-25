package meteo.icing.era;

import java.util.HashMap;
import java.util.Map;

public class CAMSConf
{

	static Map <String, String> CAMS_HEADER = new HashMap <> ();
	static {
		CAMS_HEADER.put("class","mc");
		CAMS_HEADER.put("dataset", "cams_nrealtime");
		CAMS_HEADER.put("expver","0001");
		CAMS_HEADER.put("stream","oper");
		CAMS_HEADER.put("format", "netcdf");
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


}
