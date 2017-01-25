package meteo.icing.satellites;

import java.io.File;

import ucar.nc2.dataset.NetcdfDataset;

public class HDFReadTest
{
	public static void main(String[] args) throws Exception
	{

		String filename = "E:/Development/workspaces/meteo/meteo.icing/data/satellite/2010025005412_19917_CS_2C-ICE_GRANULE_P1_R04_E03.hdf";

		File file = new File( filename );

		NetcdfDataset ncd = NetcdfDataset.openDataset( file.getAbsolutePath() );

		System.out.println(ncd);
	}
}
