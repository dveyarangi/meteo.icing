package meteo.icing.era;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.ecmwf.APIError;
import org.ecmwf.DataServer;
import org.json.JSONObject;

import meteo.icing.IProgressMeter;
import meteo.icing.era.Conf.ParamGroup;

public class DownloadThread implements Runnable
{
	private Conf conf;

	private DataServer server;
	private DataStamp stamp;

	private IProgressMeter meter;

	private boolean stampProblem;

	public DownloadThread(Conf conf, DataStamp stamp, IProgressMeter meter)
	{
		this.conf = conf;
		this.server = new DataServer();
		this.stamp = stamp;
		this.stampProblem = false;
		this.meter = meter;
	}

	@Override
	public void run()
	{
//		System.out.println("***********");
		Map<String, String> headers = conf.getHeaders();
		ParamGroup [] groups = conf.getGroups();

		stampProblem = false;

		groups: for(ParamGroup group :  groups)
		{
			Map <String, String> attributes = group.marsAttributes;
			Map <String, String> params = group.params;

			boolean skipHour = true;
			for( int hour : group.hours ) //skip not specified base hours:
				if ( stamp.timestamp.getHourOfDay() == hour) skipHour = false;

			if( skipHour) continue groups;

			for( int step : group.steps)
			{
				// fix timestamp if using forecasted level, for example, write 00Z + 6 hours forecast as 06Z

				long origTimestamp = stamp.timestamp.getMillis();

				String origDateStr = DataStamp.toDateStr(origTimestamp);
				String origTimeStr = DataStamp.toTimeStr(origTimestamp);

				long fixedTimestamp = group.toDataTime(stamp.timestamp.getMillis(), step);


				String fixedMonthStr = DataStamp.toMonthStr(fixedTimestamp);
				String fixedDayStr = DataStamp.toDayStr(fixedTimestamp);
				String fixedTimeStr = DataStamp.toTimeStr(fixedTimestamp);
				String fixedDateStr = DataStamp.toDateStr(fixedTimestamp);

				String datetimeStr =  fixedDateStr + "_" + fixedTimeStr + "Z";
				String path  = conf.eraDir + "/" + fixedMonthStr + "/" + fixedDayStr;

				new File(path).mkdirs();

				for(String paramName : params.keySet())
				{
					String filename = path + "/" + datetimeStr + "_" + paramName +"_"+ group.name + ".nc";
					File file = new File( filename );


					if( file.exists() && file.length() > 0)
						continue;

					JSONObject request = new JSONObject();

					for(String headerKey : headers.keySet())
						request.put( headerKey, headers.get( headerKey ));

					request.put("param", params.get(paramName));
					request.put("step", step );
					request.put("date", origDateStr );
					request.put("time", origTimeStr + ":00:00" );
					request.put("target",filename);

					for(Map.Entry <String, String> entry : attributes.entrySet())
					{
						request.put( entry.getKey(), entry.getValue() );
					}


					meter.setProgress( 0, "Connecting..." );
					try {
						server.retrieve(request, meter);
					}
					catch( APIError | IOException e )
					{
						e.printStackTrace();
						stampProblem = true;
					}
				}
			}
		}
	}

	public boolean isFailed() { return stampProblem; }

	public DataStamp stamp()
	{
		return stamp;
	}

	public IProgressMeter getProgressMeter()
	{
		return meter;
	}


}
