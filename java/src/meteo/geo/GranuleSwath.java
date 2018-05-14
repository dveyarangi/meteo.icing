package meteo.geo;

import java.io.IOException;

import lombok.Getter;
import ucar.ma2.Array;
import ucar.nc2.Group;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

public class GranuleSwath implements ISwath
{
	private NetcdfDataset ncd;

	@Getter Array lats;
	@Getter Array lons;
	@Getter Array heights;
	@Getter Array times;

	public GranuleSwath(NetcdfDataset ncd) throws IOException
	{
		this.ncd = ncd;

		Group productRoot = ncd.getRootGroup().getGroups().get(0);

		Group geoGroup = productRoot.findGroup("Geolocation_Fields");
		Group swathGroup = productRoot.findGroup("Swath_Attributes");

		Variable latVar = geoGroup.findVariable("Latitude");
		Variable lonVar = geoGroup.findVariable("Longitude");
		Variable heightVar = geoGroup.findVariable("Height");
		Variable timeVar = geoGroup.findVariable("Profile_time");

		lats = latVar.read();
		lons = lonVar.read();
		heights = heightVar.read();
		times = timeVar.read();
		
		float factor = (Float)swathGroup.findAttribute("Height.factor").getValue(0);
		float offset = (Float)swathGroup.findAttribute("Height.offset").getValue(0);

		System.out.println(factor + " : " + offset);
	}

}
