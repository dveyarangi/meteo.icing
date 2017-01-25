package meteo.icing;
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

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import meteo.icing.era.Conf;
import meteo.icing.era.DataStamp;
import meteo.icing.era.DownloadThread;

public class Main {

	/**
	 * Run the downloader
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Main d = new Main();
		d.run();
	}

	private class DownloadCallback implements Runnable
	{

		private DownloadThread downloader;

		public DownloadCallback(DownloadThread downloader)
		{
			this.downloader = downloader;
		}

		@Override
		public void run()
		{
			downloader.run();
			if( downloader.isFailed())
			{
				try
				{
					Thread.sleep(5000);
				} catch( InterruptedException e ) { e.printStackTrace(); }
				stamps.add( downloader.stamp());
			}

			Integer id = downloader.getProgressMeter().bar();
//			System.out.println(id);
			synchronized(barPool)
			{
				barPool.add( id );
			}
		}

	}

	Queue <Integer> barPool = new LinkedList <Integer> ();

	private Queue <DataStamp> stamps = new LinkedList <> ();
	private ExecutorService executor;

	public static int THREADS = 12;

	public void run() {

		Conf conf = Conf.ERA_INTERIM;

		Console console = new Console( THREADS );
		console.show();

		this.executor = Executors.newFixedThreadPool( THREADS );

		for(int t = 0; t < THREADS; t ++) barPool.add( new Integer(t) );

		DateTimeZone.setDefault(DateTimeZone.UTC);
		DateTime startingTime = new DateTime( 2016, 5, 30, 0, 0 );
		DateTime endingTime = new DateTime( 1996, 1, 1, 0, 0 );


		for(DateTime datetime = startingTime ;
			datetime.isAfter( endingTime);
			datetime = datetime.plusHours(-6) )
		{


			DataStamp stamp = new DataStamp( datetime );
			stamps.add( stamp );
		}

		stamp:while(!stamps.isEmpty())
		{
			if(barPool.isEmpty())
			{
				try
				{
					Thread.sleep(100);
				} catch( InterruptedException e )
				{
					e.printStackTrace();
				}
				continue stamp;
			}

			DataStamp stamp = stamps.poll();

			Integer bar;
			synchronized(barPool)
			{
				bar = barPool.poll();
			}
			DownloadCallback downloader = new DownloadCallback( new DownloadThread( conf, stamp, console.getProgressMeter( bar ) ) );

			executor.execute(downloader);

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
