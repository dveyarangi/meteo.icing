package meteo.icing;

public interface IProgressMeter
{

	void setProgress( int value, String label );

	Integer bar();

}
