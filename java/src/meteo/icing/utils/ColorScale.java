package meteo.icing.utils;


import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;

import lombok.Getter;
import meteo.icing.utils.ColorScaleConf.ColoredValue;

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

    public static ColorCreator <com.badlogic.gdx.graphics.Color> LIBGDX_CREATOR = (r,g,b,a) -> new com.badlogic.gdx.graphics.Color(r,g,b,a);
    public static ColorCreator <java.awt.Color>                     AWT_CREATOR = (r,g,b,a) -> new java.awt.Color(r,g,b,a);

    public ColorCreator <com.badlogic.gdx.graphics.Color> DEFAULT_CREATOR = LIBGDX_CREATOR;


	/**
	 * Minimal input value; values below it will result in the same color
	 */
	private float min;

	/**
	 * Maximal input value; values above it will result in the same color
	 */
	private float max;

	/**
	 * List of colors and their matching normalized values.
	 */
	private List <ColoredValue> colors;

	/**
	 * Confuguration
	 */
	@Getter private ColorScaleConf conf;



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
	public ColorScale(float min, float max, List <ColoredValue> colors)
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
	/**
	 * @param value to get color for
	 * @param requestedAlpha alpha of the resulting color; keeps colorscale's alpha if negative
	 * @param result ing color
	 * @return
	 */	
	public <C> C toColor(float value, ColorCreator <C> result) 
	{ 
		Color color;
		if( Float.isNaN( value ))
		{
			color = conf.empty;
			return result.toColor( color.r, color.g, color.b, color.a );
		}
		
		int colorNum = colors.size();

		if( value <= min || value < colors.get( 0 ).value)
		{
			// value below minimum for this colorscale
			color = this.colorBelow( 0 );
			return result.toColor( color.r, color.g, color.b, color.a );
		}
		
		if( value >= max || value > colors.get( colors.size()-1 ).value) // value above maximum for this colorscale
		{
			color = this.colorAbove( colorNum-1 );
			return result.toColor( color.r, color.g, color.b, color.a );
		}
		// picking color
		float normVal = ( value - min ) / (max-min);

		// locate lower index:
		int idx = 0;
		while( idx < colorNum-1 )
		{
			if( colors.get( idx ).getValue() <= normVal 
			 && colors.get(idx+1).getValue() > normVal )
				break;
			idx ++;
		} 
		
		ColoredValue cv1 = colors.get( idx );
		float p1 = cv1.getValue();
		
		if( p1 == normVal                // value is at exact color
		 || idx >= colorNum-1       // last color in the list
		 || ! conf.useInterpolation )    // not interpolating
		{
			color = cv1.getColor();
			return result.toColor( color.r, color.g, color.b, color.a );
		}
		
		// interpolate!
		ColoredValue cv2 = colors.get( idx+1);
		float p2 = cv2.getValue();
		
		Color lower = cv1.getColor();
		Color upper = cv2.getColor();
		
		float dc = (normVal - p1) / (p2 - p1);
//		System.out.println(dc);

		float r = (lower.r * (1-dc) + upper.r * dc);
		float g = (lower.g * (1-dc) + upper.g * dc);
		float b = (lower.b * (1-dc) + upper.b * dc);
		float a = (lower.a * (1-dc) + upper.a * dc);
			
		return result.toColor( r, g, b, a );

	}

	/**
	 * Retrieves color for values just below given index.
	 */
	private Color colorBelow( int idx )
	{
		if( idx <= 0 )
			return conf.belowScale != null ? conf.belowScale  // has configured color for infimum values ?
						 			         : colors.get( this.toColorIdx( 0 )).getColor();  // use lowest value from the scale
		else
			return colors.get( this.toColorIdx( idx-1 )).getColor();
	}
	/**
	 * Retrieves color for value just above given index.
	 */
	private Color colorAbove( int idx )
	{
		if( idx >= colors.size()-1)
			return conf.aboveScale != null ? conf.aboveScale
										   : colors.get( this.toColorIdx( colors.size()-1 )).getColor();
		else
			return colors.get( this.toColorIdx( idx+1 )).getColor();
	}

	public java.awt.Color toAWTColor( float value )
	{
		return this.toColor( value, AWT_CREATOR );
	}
	public Color toGDXColor( float value )
	{
		return this.toColor( value, LIBGDX_CREATOR );
	}

}
