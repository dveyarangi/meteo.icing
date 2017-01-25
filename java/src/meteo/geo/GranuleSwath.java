package meteo.geo;

import java.io.IOException;

import ucar.ma2.Array;
import ucar.nc2.Group;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

public class GranuleSwath implements ISwath
{
	private NetcdfDataset ncd;

	Array lats;
	Array lons;

	public GranuleSwath(NetcdfDataset ncd) throws IOException
	{
		this.ncd = ncd;

		Group productRoot = ncd.getRootGroup().getGroups().get(0);

		Group geoGroup = productRoot.findGroup("Geolocation_Fields");

		Variable latVar = geoGroup.findVariable("Latitude");
		Variable lonVar = geoGroup.findVariable("Longitude");

		lats = latVar.read();
		lons = lonVar.read();
	}
	@Override
	public Array getLats() { return lats; }
	@Override
	public Array getLons() { return lons; }

}
