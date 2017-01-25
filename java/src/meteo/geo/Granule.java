package meteo.geo;

import java.io.IOException;

import ucar.ma2.ArrayFloat;
import ucar.nc2.Group;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

public class Granule
{
	private final NetcdfDataset ncd;

	public final ISwath swath;

	public Granule( NetcdfDataset ncd)
	{
		this.ncd = ncd;
		try
		{
			this.swath = new GranuleSwath( ncd );
		}
		catch( IOException e ) { throw new RuntimeException(e); }

	}

	public ArrayFloat.D2 read( String parameter )
	{
		Group productRoot = ncd.getRootGroup().getGroups().get(0);


		Group dataGroup = productRoot.findGroup("Data_Fields");

		Variable var = dataGroup.findVariable(parameter);

		// Array iceWaterData = iceWaterVar.read(new int [] {0, 0},
		// iceWaterVar.getShape());
		ArrayFloat.D2 values;
		try
		{
			values = (ArrayFloat.D2) var.read();
		} catch( IOException e ) { throw new RuntimeException(e); }

		return values;
	}

}
