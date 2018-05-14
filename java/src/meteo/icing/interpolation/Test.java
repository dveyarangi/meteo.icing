package meteo.icing.interpolation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import meteo.icing.era.Conf;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.unidata.geoloc.LatLonPoint;

public class Test 
{
	public static void main(String[] args) throws Exception
	{
		loadNatives();
		
		Conf conf = Conf.ERA_INTERIM;
		
		
//		CloudsatIndex satIndex = new CloudsatIndex(conf);
//		EraIndex eraIndex = new EraIndex(conf);

/*		String satfilename = conf.satDir + "/2010-01/28/2010028080036_19965_CS_2B-GEOPROF-LIDAR_GRANULE_P2_R04_E03.hdf";
		File satfile = new File( satfilename );

		NetcdfDataset satncd = NetcdfDataset.openDataset( satfile.getAbsolutePath() );
		ISwath swath = new GranuleSwath(satncd);
		Array lats = swath.getLats();
		Array lons = swath.getLons();
		
		System.out.println( satncd );*/
		
		String ecmfilename = conf.eraDir + "/2016-09/06/2016-09-06_00Z_U_isobaric.nc";
		File ecmfile = new File( ecmfilename );

		NetcdfDataset ecmncd = NetcdfDataset.openDataset( ecmfile.getAbsolutePath() );
		GridDataset ecmwfGridset = new GridDataset(ecmncd);
		System.out.println( ecmncd );
		
		String outPath = "test.nc4";
		
		NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4, outPath);

		Group rootGroup = null;
		
		List <Dimension> dims = new ArrayList <> (4);
		for( Dimension dim : ecmncd.getDimensions() )
		{
			Dimension newDim = writer.addDimension( rootGroup, dim.getFullName(), dim.getLength() );
			
			dims.add( newDim );
		}
		
		GridDatatype gpGrid = ecmwfGridset.findGridDatatype("z");
		
		GridCoordSystem gcs = gpGrid.getCoordinateSystem();

		
		CoordinateAxis xAxis = gcs.getXHorizAxis();
		CoordinateAxis yAxis = gcs.getYHorizAxis();
		CoordinateAxis zAxis = gcs.getVerticalAxis();
		
		double minlat = Double.MAX_VALUE, minlon = Double.MAX_VALUE,maxlat = Double.MIN_VALUE,maxlon = Double.MIN_VALUE;
		for( int xidx = 0; xidx < xAxis.getSize(); xidx ++)
			for( int yidx = 0; yidx < yAxis.getSize(); yidx ++)
			{
				LatLonPoint point = gcs.getLatLon(xidx, yidx);
				if( minlat > point.getLatitude()) minlat = point.getLatitude();
				if( minlon > point.getLongitude()) minlon = point.getLongitude();
				if( maxlat < point.getLatitude()) maxlat = point.getLatitude();
				if( maxlon < point.getLongitude()) maxlon = point.getLongitude();
				
				///////////////////////////////////////////////////////////////////////////////////
				// determine grid cell size
				double plat = point.getLatitude(); double plon = point.getLongitude();
				double nlat = point.getLatitude(); double nlon = point.getLongitude();
				if( xidx > 0 && yidx > 0)
				{
					LatLonPoint minmin = gcs.getLatLon(xidx-1, yidx-1);
					plat = minmin.getLatitude(); plon = minmin.getLongitude();
				}
				
				if( xidx < xAxis.getSize()-1 && yidx < yAxis.getSize()-1)
				{
					LatLonPoint maxmax = gcs.getLatLon(xidx+1, yidx+1);
					nlat = maxmax.getLatitude(); nlon = maxmax.getLongitude();
				}
				
				double latspan = Math.abs(nlat - plat)/2;
				double lonspan = Math.abs(nlon - plon)/2;
				
				///////////////////////////////////////////////////////////////////////////////////
				// find best matching swath position:
				double minDistSqr = Double.MAX_VALUE;
				int minDistIndex = -1;
				/*
				for(int sidx = 0; sidx < lats.getSize(); sidx ++)
				{
					float satlat = lats.getFloat(sidx);
					float satlon = lons.getFloat(sidx);
					
					double distSqr = Math.pow((point.getLatitude() - satlat), 2) +
									Math.pow((point.getLongitude() - satlon), 2);
					if( distSqr < minDistSqr )
					{
						minDistSqr = distSqr;
						minDistIndex = sidx;
					}
				}
				
				float satlat = lats.getFloat(minDistIndex);
				float satlon = lons.getFloat(minDistIndex);
				
				if( Math.abs( point.getLatitude()  - satlat) < latspan 
				 && Math.abs( point.getLongitude() - satlon) < lonspan )
				{
					System.out.println(point + " ::: " + satlat + "," + satlon);
				}*/
			}
		
//		System.out.println(minlat);
//		System.out.println(minlon);
//		System.out.println(maxlat);
//		System.out.println(maxlon);

		try {
			writer.create();
		}
		catch(IOException e) { throw new RuntimeException(e); }
		finally { writer.close(); }
	}
	
	private static void loadNatives()
	{
		File nativesRoot = new File("java/lib/natives/");
		
		Queue <File> libQueue = new LinkedList <> ();
		for(File lib : nativesRoot.listFiles()) // listing natives dir into queue:
			libQueue.add( lib );
		
		while( ! libQueue.isEmpty() )
		{
			File lib = libQueue.poll(); 
			try {
				System.load(lib.getAbsolutePath());
			}
			catch( UnsatisfiedLinkError e )
			{   // if failed, try again later:
				libQueue.add( lib );
			}
		}
	}
}
