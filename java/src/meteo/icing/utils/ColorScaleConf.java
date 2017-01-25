package meteo.icing.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import javax.security.auth.login.Configuration;

import com.badlogic.gdx.graphics.Color;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

public class ColorScaleConf
{
	public String name;
	/**
	 * Marks zero index in the colors array above
	 * and makes the colorscale cyclic
	 */
	public int cyclicOrigin = -1;

	/**
	 * Whether values between the gradation should be linearily interpolated
	 */
	public boolean useInterpolation = false;

	/**
	 * This value and values below are use the minimal color; if NaN, minimal value is determined from the input
	 */
//		public double minCutoff = Double.NaN;

	/**
	 * This value and values above are use the maximal color; if NaN, maximal value is determined from the input
	 */
//		public double maxCutoff = Double.NaN;

	/**
	 * Colors in this list are distributed equally on the min-max interval, with color at index 0
	 * matching min value
	 */
	public List<Color> colors;

	/**
	 * Color for values below min; null to use color at min
	 */
	public Color belowScale = null;

	/**
	 * Color for values above max; null to use color at max
	 */
	public Color aboveScale = null;
	public Color error = new Color(0,0,0,0);

	public static ColorScaleConf read( String filename )
	{
		try (FileReader reader = new FileReader( new File( filename )))
		{
			return read( reader );
		} catch( IOException e )
		{
			throw new IllegalArgumentException("Failed to read " + filename, e);
		}
	}

	public static ColorScaleConf read( Reader reader ) throws IOException
	{
		Gson gson = buildGson(null);
		try
		{
			return gson.fromJson( reader, ColorScaleConf.class );
		}
		catch( JsonParseException e )
		{
			throw new IOException( e );
		}
	}

	public static Gson buildGson( Configuration conf )
	{
		Gson gson = new GsonBuilder()
				// converts class name to Class objects:
				.registerTypeAdapter(Color.class, new LibGDXColorDeserializer())
				// converts property set references to PropertySet objects:
				//.registerTypeAdapter(Map.class, new PropertySetDeserializer())
				.create();

		return gson;
	}

}
