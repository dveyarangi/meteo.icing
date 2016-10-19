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
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class ECMWFDownloader {

	/**
	 * Run the downloader
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		ECMWFDownloader d = new ECMWFDownloader();
		d.run();
	}

	private class DownloadCallback implements Runnable
	{

		private Downloader downloader;

		public DownloadCallback(Downloader downloader)
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
		}

	}

	private Queue <DataStamp> stamps = new LinkedList <> ();
	private ExecutorService executor = Executors.newFixedThreadPool(12);


	public void run() {

		DateTimeZone.setDefault(DateTimeZone.UTC);
		DateTime startingTime = new DateTime( 2016, 5, 30, 0, 0 );
		DateTime endingTime = new DateTime( 1996, 1, 1, 0, 0 );


		DateTimeFormatter dayFormat = DateTimeFormat.forPattern("dd");
		DateTimeFormatter monthYearFormat = DateTimeFormat.forPattern("yyyy-MM");
		DateTimeFormatter hourFormat = DateTimeFormat.forPattern("HH");

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

//			System.out.println(stamp.path);
		}

		stamp:while(!stamps.isEmpty())
		{
			DataStamp stamp = stamps.poll();

			DownloadCallback downloader = new DownloadCallback( new Downloader( stamp ) );

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
