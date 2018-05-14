package meteo.geo;

import ucar.ma2.Array;

public interface ISwath
{

	Array getLats();

	Array getLons();

	Array getHeights();
	
	Array getTimes();

}
