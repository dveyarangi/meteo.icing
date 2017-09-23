package meteo.geo.stations;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor @ToString
public class SurfaceStationInfo 
{
	@Getter private String stateCode;
	@Getter private String name;
	@Getter private String icao;
	@Getter private String iata;
	@Getter private String synop;
	@Getter private float lat;
	@Getter private float lon;
	@Getter private float elevation;
	@Getter private String countryCode;
	@Getter private int priority;
	
}
