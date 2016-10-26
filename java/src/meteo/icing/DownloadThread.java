package meteo.icing;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.ecmwf.APIError;
import org.ecmwf.DataServer;
import org.json.JSONObject;

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
		Map<String, Map<String, String>> paramsets = conf.getParamSets();

		stampProblem = false;
		new File(stamp.path).mkdirs();

			for(String type : paramsets.keySet())
			{
				for(String paramName : conf.getParams().keySet())
				{


					String filename = stamp.path + "/" + stamp.date + "_" + stamp.time + "Z_" + paramName +"_"+ type + ".nc";
					File file = new File( filename );


					if( file.exists() && file.length() > 0)
						continue;

					JSONObject request = new JSONObject();

					for(String headerKey : headers.keySet())
						request.put( headerKey, headers.get( headerKey ));

					request.put("param", conf.getParams().get(paramName));
					request.put("date", stamp.date );
					request.put("time", stamp.time + ":00:00" );
					request.put("target",filename);

					Map <String, String> params = paramsets.get( type );
					for(Map.Entry <String, String> entry : params.entrySet())
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
