package meteo.icing.utils;


import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;


/**
 * Maps float values to colors.
 * @author Fima
 *
 */
public class ColorScale
{
    public interface ColorCreator <C>
    {
    	C toColor(float r, float g, float b, float a);
    }

    public ColorCreator <com.badlogic.gdx.graphics.Color> LIBGDX_CREATOR = (r,g,b,a) -> new com.badlogic.gdx.graphics.Color(r,g,b,a);
    public ColorCreator <java.awt.Color>                     AWT_CREATOR = (r,g,b,a) -> new java.awt.Color(r,g,b,a);

    public ColorCreator <com.badlogic.gdx.graphics.Color> DEFAULT_CREATOR = LIBGDX_CREATOR;


	/**
	 * Minimal input value; values below it will result in the same color
	 */
	private float min;

	/**
	 * Maximal input value; values above it will result in the same color
	 */
	private float max;

	private List <Color> colors;

	private ColorScaleConf conf;



	/**
	 * @param min
	 * @param max
	 * @param isSmooth
	 * @param colors
	 */
	public ColorScale(float min, float max, ColorScaleConf conf)
	{
		this( min, max, conf.colors );
		this.conf = conf;
	}
	public ColorScale(float min, float max, List <Color> colors)
	{
		this.min = min;
		this.max = max;
		int colorNum = colors.size();

		// copy color set to faster array

		this.colors = new ArrayList <> ( colorNum );
		for(int cidx = 0; cidx < colorNum; cidx ++)
		{
			this.colors.add(cidx, colors.get( cidx ));
		}
	}

	public float min() { return min; }
	public float max() { return max; }

	private int toColorIdx(int idx)
	{
		return idx;//(idx + hueOffset) % colors.size();
	}
//	public Color toColor(float value)                           { return this.toColor( value, -1, DEFAULT_CREATOR ); }
//	public Color toColor(float value, float alpha)              { return this.toColor( value, alpha, DEFAULT_CREATOR ); }
	public <C> C toColor(float value, ColorCreator <C> creator) { return this.toColor( value, -1, creator ); }

	/**
	 * @param value to get color for
	 * @param requestedAlpha alpha of the resulting color; keeps colorscale's alpha if negative
	 * @param result ing color
	 * @return
	 */
	public <C> C toColor(float value, float requestedAlpha, ColorCreator <C> result)
	{
		if( Float.isNaN( value ))
			return  null;

		Color color;
		int colorNum = colors.size();

		if( value <= min )
			// value below minimum for this colorscale
			color = this.colorBelow( 0 );
		else
			if( value >= max) // value above maximum for this colorscale
				color = this.colorAbove( colorNum-1 );
			else
			{	// picking color
				float normVal = ( value - min ) * (colorNum-1) / (max-min);
				int floor = FastMath.floor( normVal );
				int ceil = FastMath.ceil( normVal );

				if(value == floor) // value exactly matches a color index
					color = colors.get( this.toColorIdx(floor) );
				else
					if( !conf.useInterpolation ) // use closes index
						color = colors.get( this.toColorIdx( Math.round( normVal ) ) );
					else
					{	// interpolate!
						Color lower = colors.get( floor );
						Color upper = this.colorAbove( floor );


						float dc = ceil == floor ? 0 :
							(normVal - floor) / (ceil - floor);

						float r = (lower.r * (1-dc) + upper.r * dc);
						float g = (lower.g * (1-dc) + upper.g * dc);
						float b = (lower.b * (1-dc) + upper.b * dc);
						float a = requestedAlpha < 0 ? (lower.a * (1-dc) + upper.a * dc) : requestedAlpha;
						return result.toColor( r, g, b, a );
					}
			}

		float alpha = requestedAlpha < 0 ? color.a : requestedAlpha;

		return result.toColor( color.r, color.g, color.b, alpha );
	}

	/**
	 * Retrieves color for values just below given index.
	 */
	private Color colorBelow( int idx )
	{
		if( idx <= 0 )
			return conf.belowScale != null ? conf.belowScale  // has configured color for infimum values ?
						 			         : colors.get( this.toColorIdx( 0 ));  // use lowest value from the scale
		else
			return colors.get( this.toColorIdx( idx-1 ));
	}
	/**
	 * Retrieves color for value just above given index.
	 */
	private Color colorAbove( int idx )
	{
		if( idx >= colors.size()-1)
			return conf.aboveScale != null ? conf.aboveScale
										   : colors.get( this.toColorIdx( colors.size()-1 ));
		else
			return colors.get( this.toColorIdx( idx+1 ));
	}

	public java.awt.Color toAWTColor( float value )
	{
		return this.toColor( value, AWT_CREATOR );
	}
//	public ColorCreator getCreator() { return creator; }


}
