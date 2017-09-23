package meteo.icing.satellites;

import java.io.File;

import ucar.nc2.dataset.NetcdfDataset;

public class HDFReadTest
{
	public static void main(String[] args) throws Exception
	{

		String satfilename = "H:/icing/Dropbox/icing/archive/cloudsat/2010-01/28/2010028012504_19961_CS_2B-GEOPROF-LIDAR_GRANULE_P2_R04_E03.hdf";
		File satfile = new File( satfilename );

		NetcdfDataset satncd = NetcdfDataset.openDataset( satfile.getAbsolutePath() );
		System.out.println( satncd );
		
		String ecmfilename = "H:/icing/Dropbox/icing/archive/2016-09/06/2016-09-06_00Z_T_isobaric.nc";
		File ecmfile = new File( ecmfilename );

		NetcdfDataset ecmncd = NetcdfDataset.openDataset( ecmfile.getAbsolutePath() );
		System.out.println( ecmncd );

	}
}
