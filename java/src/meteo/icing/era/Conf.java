package meteo.icing.era;

import java.util.HashMap;
import java.util.Map;

public class Conf
{
	public static String FORMAT_GRIB = "grib";
	public static String FORMAT_NETCDF = "netcdf";
	private static String FORMAT = FORMAT_NETCDF;


	public static String LEVELS_ALL = "1/2/3/5/7/10/20/30/50/70/100/125/150/175/200/225/250/300/350/400/450/500/550/600/650/700/750/775/800/825/850/875/900/925/950/975/1000";
	public static String LEVELS_ICING = "100/150/200/250/300/350/400/450/500/550/600/650/700/750/800/850/900/950/1000";

	static Map <String, Map <String, String>> ERA_INTERIM_REQUESTS = new HashMap <> ();
	static Map <String, Map <String, String>> ERA_INTERIM_PARAM_SETS = new HashMap <> ();

	public static Map <String, String> ERA_INTERIM_SURFACE_FORECASTED_PARAMS = new HashMap <String, String> ();
	static {

		ERA_INTERIM_PARAM_SETS.put("sfc12", ERA_INTERIM_SURFACE_FORECASTED_PARAMS);
	}

	public static abstract class ParamGroup
	{
		Map <String, String> params = new HashMap <String, String> ();

		int [] hours;

		Map <String, String> marsAttributes = new HashMap <> ();

		String name;

		int [] steps;

		public abstract long toDataTime( long timestamp, int step );
	}

	public static class IsobaricAnalyticParamGroup extends ParamGroup
	{
		public IsobaricAnalyticParamGroup( String levelsList )
		{
			name = "isobaric";

			params.put("T", "130.128");
			params.put("W", "135.128");
			params.put("RH", "157.128");
			params.put("CC", "248.128"); // cloud cover
			params.put("DIV", "155.128"); // divergence
			params.put("GP", "129.128"); // geopotential
			params.put("PV", "60.128"); // potential vorticity
			params.put("CIWC", "247.128"); // specific ice water content
			params.put("CLWC", "246.128"); // specific liquid water content
			params.put("U", "131.128"); // u wind comp
			params.put("V", "132.128"); // v wind comp
			params.put("Q", "133.128"); // specific humidity
			params.put("VO", "138.128"); // Relative vorticity

			marsAttributes.put("step", "0");
			marsAttributes.put("levtype", "pl");
			marsAttributes.put("levelist", levelsList );
			marsAttributes.put("type","an");

			hours = new int [] { 00, 06, 12, 18};

			steps = new int [] { 0 };
		}

		@Override
		public long toDataTime( long timestamp, int step ) { return timestamp; }
	};

	public static class SurfaceAnalyticParamGroup extends ParamGroup
	{
		public SurfaceAnalyticParamGroup()
		{
			name = "sfc";

			params.put("MSLP", "151.128"); // mean sea level pressure
			params.put("TCWV", "137.128"); // total column water vapor
			params.put("TCW", "136.128"); // total column water
			params.put("VIIW", "57.162"); // vertical integral of frozen water
			params.put("VILW", "56.162"); // vertical integral of liquid water
			params.put("VIWV", "55.162"); // vertical integral of water vapor
			params.put("HCC", "188.128"); // high cloud cover
			params.put("MCC", "187.128"); // medium cloud cover
			params.put("LCC", "186.128"); // low cloud cover
			params.put("TCC", "164.128"); // total cloud cover

			marsAttributes.put("step", "0");
			marsAttributes.put("levtype", "sfc");
			marsAttributes.put("type","an");

			hours = new int [] { 00, 06, 12, 18};
			steps = new int [] { 0 };
		}

		@Override
		public long toDataTime( long timestamp, int step ) { return timestamp; }
	}

	public static class SurfaceForecasted12ParamGroup extends ParamGroup
	{
		public SurfaceForecasted12ParamGroup()
		{
			name = "sfc12" ;

			params.put("LSP", "142.128"); // large scale precipitation // step 3
			params.put("LSPF", "50.128"); // large scale precipitation fraction// step 3
			params.put("TP", "228.128"); // total precipitation // step 3
			params.put("CAPE", "59.128"); // convective available potential energy
			params.put("BLH", "159.128"); // boundary layer height
			params.put("TCLW", "78.128"); // total column liquid water
			params.put("CP", "143.128"); // convective precipitation

			marsAttributes.put("step", "6");
			marsAttributes.put("levtype", "sfc");
			marsAttributes.put("type","fc");

			hours = new int [] { 00, 12 };

			steps = new int [] { 6, 12 };
		}

		@Override
		public long toDataTime( long timestamp, int step ) { return timestamp + step * 60 * 60 * 1000; }

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

	public static final Conf ERA_INTERIM = new Conf("H:/icing/Dropbox/icing/archive");



	static {
		ERA_INTERIM.headers = ERA_INTERIM_HEADER;
		ERA_INTERIM.groups = new ParamGroup [] {
				new SurfaceForecasted12ParamGroup(),
				new IsobaricAnalyticParamGroup( LEVELS_ICING ),
				new SurfaceAnalyticParamGroup(),
		};
	}

	///////////////////////////////////////////////////////////////////////////////

	public String rootDir;

	public String format = FORMAT_NETCDF;

	public Map<String, String> headers;
	public ParamGroup [] groups;

	public Conf(String rootDir)
	{
		this.rootDir = rootDir;
	}

	public Map<String, String> getHeaders() { return headers; }

	public ParamGroup [] getGroups() { return groups; }


}
