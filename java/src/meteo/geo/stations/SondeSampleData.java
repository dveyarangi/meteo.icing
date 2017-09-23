package meteo.geo.stations;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor @ToString
public class SondeSampleData 
{
	@Getter private float pressure;
	@Getter private float height;
	@Getter private float temperature;
	@Getter private float dewpointTemperature;
	@Getter private float relativeHumidity;
	@Getter private float mixingRatio;
	@Getter private float windDirection;
	@Getter private float windSpeed;
	@Getter private float thetaA;
	@Getter private float thetaE;
	@Getter private float thetaV;
}
