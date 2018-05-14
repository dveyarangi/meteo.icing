package meteo.geo;

import java.awt.Rectangle;
import java.awt.Shape;

public class Java2DUtil {

	public static Rectangle getBoundingBox(Shape ... shapes) 
	{
		Rectangle bb = new Rectangle();
		for( Shape shape : shapes )
		{
			Rectangle sb = shape.getBounds();
			
			if( bb.x > sb.getX() )
				bb.x = sb.x;
			
			if( bb.x > sb.x )
				bb.x = sb.x;
			
			if( bb.x+bb.width < sb.x+sb.width )
				bb.width = sb.x+sb.width-bb.x;
			
			if( bb.y+bb.height < sb.y+sb.height )
				bb.height = sb.y+sb.height-bb.y;
			
		}
		
		return bb;
	}

}
