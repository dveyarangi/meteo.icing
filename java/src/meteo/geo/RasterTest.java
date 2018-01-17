package meteo.geo;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import org.geotools.data.DataUtilities;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;

import com.badlogic.gdx.math.Vector2;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

import meteo.icing.utils.BufferedCanvas;
import meteo.icing.utils.ColorScale;
import meteo.icing.utils.ColorScaleConf;
import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayShort;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

public class RasterTest
{
	public static final String PARAM = TestData.parameter;
	
	public static final float REF_LAT = 32.0018f;
	public static final float REF_LON = 34.8297f;
	
	public static NumberFormat LATLON_FMT = new DecimalFormat("0.00#");
	public static NumberFormat LEGENG_COLOR_VALUE_FMT = new DecimalFormat("#.00#");

	private static int axisFontSize = 14;
	public static final SimpleDateFormat NDS_FMT = new SimpleDateFormat("yyyyMMddHHmmss");
	public static final SimpleDateFormat OUT_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static void main( String[] args ) throws Exception
	{
		///////////////////////////////////////////////////////////


		ColorScaleConf colorConf = ColorScaleConf.read("E:/Development/workspaces/meteo/meteo.icing/etc/PurpleFire");
		ColorScale scale = new ColorScale(0, 1, colorConf);

		int swathIdx = 0;
		for( String filename : TestData.FILENAMES )
		{

			File satfile = new File(filename);

			NetcdfDataset ncd = NetcdfDataset.openDataset(satfile.getAbsolutePath());
			Group productRoot = ncd.getRootGroup().getGroups().get(0);


			ISwath swath = new GranuleSwath( ncd );
			Array lats = swath.getLats();
			Array lons = swath.getLons();
			
			Array heights = swath.getHeights();
			int arrayHeight = heights.getShape()[1];
			float maxHeight = 30000;
			
			
			//////////////////////////////////////////////////////////////////
			// locate index closest to REF_LAT and LON
			
			float minDist = Float.POSITIVE_INFINITY;
			int closestIdx = -1;
			
			for(int pidx = 0; pidx < lats.getSize(); pidx ++)
			{
				float lat = lats.getFloat(pidx);
				float lon = lons.getFloat(pidx);
				
				float distSquare = (REF_LAT-lat)*(REF_LAT-lat) + (REF_LON-lon)*(REF_LON-lon);
				if( distSquare < minDist )
				{
					minDist = distSquare;
					closestIdx = pidx;
				}
			}
			
			if( minDist > 500)
			{
				System.out.println("No near points for this data");
				return;
			}
			
			
			

			Group dataGroup = productRoot.findGroup("Data_Fields");
			Group swathGroup = productRoot.findGroup("Swath_Attributes");
			// debug-print data
			//printData(dataGroup);
			
			// iceWaterData.

			int yscale = 4;
			int pathSpan = 1000;
			
			int rowHeight = yscale*arrayHeight;
			int arrayWidth = 2*pathSpan;
			Rectangle swathFrame = new Rectangle(0, 0, rowHeight, rowHeight);
			Rectangle graphFrame = new Rectangle(swathFrame.x+swathFrame.width, 0, arrayWidth, rowHeight);
			
//			Rectangle swathFrame = new Rectangle(w, 0, w+h, h+labelYOffset);
			Rectangle footerFrame = new Rectangle(0, rowHeight, graphFrame.width, 110);
			
			int labelYOffset = 70;
			Rectangle canvasFrame = Java2DUtil.getBoundingBox(graphFrame, swathFrame, footerFrame);
			Variable paramVar = dataGroup.findVariable(PARAM);
			
		//for(Variable paramVar : dataGroup.getVariables())
		{
			String paramName = paramVar.getShortName();
			
			if(paramVar.getShape().length != 2)
				continue;
			
			String paramLongName = swathGroup.findAttribute(paramName + ".long_name").getStringValue();
			String paramUnits  = swathGroup.findAttribute(paramName + ".units").getStringValue();
			String stringMissop  = swathGroup.findAttribute(paramName + ".missop").getStringValue();
			float missingValue = getMissingValue( swathGroup, paramName ) ;
			
			float factor = (Float)swathGroup.findAttribute(paramName + ".factor").getValue(0);
			float offset = (Float)swathGroup.findAttribute(paramName + ".offset").getValue(0);
			Array paramValues = paramVar.read();
			
			System.out.println(paramName + " " + factor + " : " + offset);
			
//			if( true )
//				continue;
			
			BufferedCanvas canvas = new BufferedCanvas( canvasFrame.width, canvasFrame.height );
			Graphics2D g2d = canvas.g2d;
			
			g2d.setFont(new Font("CourierNew", Font.PLAIN, axisFontSize )); 
			
			float min = Float.POSITIVE_INFINITY;
			float max = Float.NEGATIVE_INFINITY;
			
			int w = graphFrame.width;
			int h = graphFrame.height;
			
			for(int x = 0; x <= w; x ++)
				for(int yidx = 0; yidx < arrayHeight; yidx ++)
				{
					int pidx = closestIdx + x - pathSpan;
					
					float value = getValue( paramValues, pidx, yidx, missingValue );
					if(Float.isNaN( value )) continue;
					if( value > max ) 
						max = value;
					if( value < min ) min = value;
				}
//			System.out.println(min + " ::: " + max);
		//	min = 0; // TODO: check!
			//max = 1000;
			scale = new ColorScale(0, 1, colorConf);
			int gox = graphFrame.x;
			int goy = graphFrame.y;
			for(int xidx = 0; xidx <= graphFrame.width; xidx ++)
				for(int yidx = 0; yidx < arrayHeight; yidx ++)
				{
					int pidx = closestIdx + xidx - pathSpan;
					int x = gox + xidx;
					float y = (goy + h-(h*(heights.getFloat(yidx)+120)/maxHeight));
					float y2 = (goy+ h-(h*(heights.getFloat(yidx)-120)/maxHeight));
					//float y2 = yidx+1 == arrayHeight ? 0: h-(h*heights.getFloat(yidx+1)/maxHeight);
					
					float rawValue = getValue( paramValues, pidx, yidx, missingValue );
					
					float value = rawValue / (max-min) - min;
					Color color = scale.toAWTColor(value);
					if( Float.isNaN( rawValue ) || color == null)
					{
						canvas.g2d.setColor(Color.DARK_GRAY);
						canvas.g2d.drawRect(x, (int)y, 1, (int)(y2-y));
					}
					else
					{
						canvas.g2d.setColor(scale.toAWTColor(value));
						canvas.g2d.drawRect(x, (int)y, 1, (int)(y2-y));
					}
				}

			renderVericalGrid(g2d, lats, lons, graphFrame, closestIdx, pathSpan);
			g2d.setColor(Color.WHITE);
			g2d.drawRect(graphFrame.x, graphFrame.y, graphFrame.width, graphFrame.height-1);
			
			String capsuleStartTimeStr = ncd.getRootGroup().findAttribute("start_time").getStringValue();
			long capsuleStartTime = NDS_FMT.parse( capsuleStartTimeStr ).getTime();
			long startTime = capsuleStartTime + 1000*Math.round( swath.getTimes().getFloat(closestIdx) );
			String fileTimestamp = NDS_FMT.format( startTime );
			String labelTimestamp = OUT_FMT.format( startTime );
			
			g2d.setFont(new Font("CourierNew", Font.PLAIN, 28)); 
			g2d.drawString(  paramLongName, footerFrame.x+10,footerFrame.y+36);
			
			g2d.drawString( "Capsule start: " +  OUT_FMT.format( capsuleStartTime ), footerFrame.x+10, footerFrame.y+66);
			g2d.drawString( "Nearest time : " +  OUT_FMT.format( startTime ), footerFrame.x+10, footerFrame.y+96);
			
			renderColorscale(g2d, paramUnits, 
					new Rectangle(
							graphFrame.x+1*graphFrame.width/6,
							footerFrame.y+10,
							graphFrame.width*2/3,
							footerFrame.height*2/3), 
					scale, min, max );
			
			
			g2d.setColor(Color.WHITE);
			g2d.drawRect(0, 0, canvasFrame.width-1, canvasFrame.height-1);

			renderSwath(g2d, swathFrame, ncd, closestIdx, pathSpan);
			g2d.setColor(Color.WHITE);
			g2d.drawRect(swathFrame.x, swathFrame.y, swathFrame.width, swathFrame.height-1);
		/////////////////////////////////////////////////
			// flush image to disk
			String imageName = "output/" + "CloudSat_"+fileTimestamp + "_" + paramName + "_Israel.png";
			System.out.println("Writing image " + imageName);
			canvas.writeImage( imageName );
			
			
			String csvFileName = "output/" + "CloudSat_"+fileTimestamp + "_" + paramName + "_Israel.csv";
			String COMMA = ",";
			try (FileWriter wri = new FileWriter(csvFileName))
			{
				for(int xidx = 0; xidx <= arrayWidth; xidx ++)
				{
					int x = closestIdx - pathSpan + xidx;
					
					long profileTime = capsuleStartTime + 1000*Math.round( swath.getTimes().getFloat(x) );
					wri.append(COMMA).append("Time:").append(OUT_FMT.format( profileTime+ capsuleStartTime ));
				}
				wri.append("\r\n");
				for(int xidx = 0; xidx <= arrayWidth; xidx ++)
				{
					int x = closestIdx - pathSpan + xidx;
					wri.append(COMMA).append("Lat:").append(LATLON_FMT.format( lats.getFloat(x)));
				}
				wri.append("\r\n");
				for(int xidx = 0; xidx <= arrayWidth; xidx ++)
				{
					int x = closestIdx - pathSpan + xidx;
					wri.append(COMMA).append("Lon:").append(LATLON_FMT.format( lons.getFloat(x)));
				}
				wri.append("\r\n");
				for(int yidx = 0; yidx < arrayHeight; yidx ++)
				{
					wri.append(LATLON_FMT.format(heights.getFloat(yidx))).append("m").append(COMMA);
					for(int xidx = 0; xidx <= arrayWidth; xidx ++)
					{
						int x = closestIdx - pathSpan + xidx;
					
						wri.append(LATLON_FMT.format(getValue(paramValues, x, yidx, missingValue)));
						wri.append(COMMA);
					}
					wri.append("\r\n");
				}
			}
		}
		}
	}

	private static float getMissingValue(Group swathGroup, String paramName)
	{
		Number value = swathGroup.findAttribute(paramName + ".missing").getNumericValue(0);
		return value.floatValue();
	}

	private static float getValue(Array array, int pidx, int y, float missingValue) 
	{
		
		float value;
		if( array instanceof ArrayShort.D2)
			value = ((ArrayShort.D2)array).get(pidx, y);
		else
		if( array instanceof ArrayByte.D2)
			value = ((ArrayByte.D2)array).get(pidx, y);
		else
			value = ((ArrayShort.D2)array).get(pidx, y);
		
		if( missingValue < 0 && value <= missingValue)
			return Float.NaN;
		if( missingValue > 0 && value >= missingValue)
			return Float.NaN;
		return value;
	}

	private static void renderVericalGrid(Graphics2D g2d , Array lats, Array lons, Rectangle area, int closestIdx, int pathSpan) 
	{
		int ox = area.x;
		int oy = area.y;
		int w = area.width;
		int h = area.height; 
		
		// render reference location:
		Color gridColor = new Color( 0.9f, 0.9f, 0.9f, 0.5f );
		Color textColor = Color.white;
		g2d.setColor( gridColor );
		
		g2d.setFont(new Font("CourierNew", Font.PLAIN, axisFontSize )); 
		
		
		g2d.setColor( gridColor );
		g2d.drawLine(ox+pathSpan, oy+0, ox+pathSpan, oy+h);
		g2d.setColor( textColor );
		g2d.drawString("LON:"+LATLON_FMT.format(lons.getFloat(closestIdx)), ox+pathSpan+3, oy+13);
		g2d.drawString("LAT:"+LATLON_FMT.format(lats.getFloat(closestIdx)), ox+pathSpan+3, oy+27);
		
		g2d.setColor( gridColor );
		g2d.drawLine(ox+pathSpan/2, oy+0, ox+pathSpan/2, oy+h);
		g2d.setColor( textColor );
		g2d.drawString("LON:"+LATLON_FMT.format(lons.getFloat(closestIdx-pathSpan/2)), ox+pathSpan/2+3, oy+13);
		g2d.drawString("LAT:"+LATLON_FMT.format(lats.getFloat(closestIdx-pathSpan/2)), ox+pathSpan/2+3, oy+27);
		
		g2d.setColor( gridColor );
		g2d.drawLine(ox+0, oy+0, ox+0, oy+h);
		g2d.setColor( textColor );
		g2d.drawString("LON:"+LATLON_FMT.format(lons.getFloat(closestIdx-pathSpan)), ox+3, oy+13);
		g2d.drawString("LAT:"+LATLON_FMT.format(lats.getFloat(closestIdx-pathSpan)), ox+3, oy+27);
		
		g2d.setColor( gridColor );
		g2d.drawLine(ox+3*pathSpan/2, oy+0, ox+3*pathSpan/2, oy+h);
		g2d.setColor( textColor );
		g2d.drawString("LON:"+LATLON_FMT.format(lons.getFloat(closestIdx+pathSpan/2)), ox+3*pathSpan/2+3, oy+13);
		g2d.drawString("LAT:"+LATLON_FMT.format(lats.getFloat(closestIdx+pathSpan/2)), ox+3*pathSpan/2+3, oy+27);
		
		g2d.setColor( gridColor );
		g2d.drawLine(ox+w-1, oy+0, ox+w-1, oy+h);
		g2d.setColor( textColor );
		g2d.drawString("LON:"+LATLON_FMT.format(lons.getFloat(closestIdx+pathSpan)), ox+w-75, oy+13);
		g2d.drawString("LAT:"+LATLON_FMT.format(lats.getFloat(closestIdx+pathSpan)), ox+w-75, oy+27);
		
		
		float rayTop = 30000;
		int steps = 15;
		
		int rayStep = Math.round(rayTop/steps);
		
		g2d.setFont(new Font("CourierNew", Font.PLAIN, 22 )); 
		
		for(int s = 1; s < steps; s += 1)
		{
			int y = h - s * h/steps-1;
			g2d.setColor( gridColor );
			g2d.drawLine(ox+0, oy+y, ox+w, oy+y);
			
			int height = s * rayStep;
			
			g2d.setColor( textColor );
			g2d.drawString(LATLON_FMT.format(height)+"m", ox+3, oy+y+24);
		}	
	}

	private static void renderColorscale(Graphics2D g, String paramUnits, Rectangle rectangle, ColorScale scale, float min, float max) 
	{
		int axisFontSize = 22;
		g.setFont(new Font("CourierNew", Font.PLAIN, axisFontSize )); 
		
		int labelYOffset = 20+axisFontSize;

		int colorsteps = scale.getConf().colors.size();
		
		int lminx = rectangle.x;
		int lminy = rectangle.y;
		int lmaxx = rectangle.x+rectangle.width;
		int lmaxy = rectangle.y+rectangle.height/2;

		float value = scale.min();
		int tick = 0;
		for(int x = lminx; x <= lmaxx; x += 1)
		{
			float colorValue = (float)(x - lminx) / (lmaxx-lminx);
			value = colorValue * (max - min) + min;
			Color binColor = scale.toColor( colorValue, ColorScale.AWT_CREATOR );
			g.setColor(binColor);
			g.fillRect(x, lminy, 1, lmaxy-lminy);
			if( tick % ((lmaxx-lminx) / 10) == 0)
			{
				g.setColor(Color.WHITE);
				g.drawRect(x, lmaxy, 1, 5);
				
				String label = LEGENG_COLOR_VALUE_FMT.format(value);
				g.drawString( label, x-axisFontSize*label.length()/4, lmaxy+axisFontSize+2);
			}
			tick ++;
		}

		if( !paramUnits.equals("--"))
			g.drawString( paramUnits, lmaxx+axisFontSize/2, (lmaxy+lminy)/2+axisFontSize/2-1);
		
		g.setColor(Color.WHITE);
		g.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height/2);
	}	

	private static void printData(Group dataGroup) 
	{

		for(Variable var : dataGroup.getVariables())
		{
			System.out.print(var.getShortName() + " ");
			if(var.getShape().length != 0)
			{
				System.out.print("(");
				for(int i = 0; i < var.getShape().length; i ++)
				{
					int dimSize = var.getShape(i);
					Dimension dim = var.getDimension(i);
					System.out.print(dim.getDODSName() + "=" + dimSize +" ");
				}
				System.out.print(")");
			}
			else
				System.out.print("(scalar)");
			if( var.getUnitsString() != null)
				System.out.print(" units: " +  var.getUnitsString());
			System.out.println();
		}
	}
	
	private static void renderSwath( Graphics2D g2d, Rectangle screenArea, NetcdfDataset ncd, int cidx, int span )
	{
		g2d.setColor(Color.WHITE);
		g2d.drawRect(screenArea.x, screenArea.y, screenArea.width, screenArea.height-1);

    	MapRenderer renderer = new MapRenderer(Color.DARK_GRAY);


		int swathIdx = 0;

		ISwath swath;
		try {
			swath = new GranuleSwath(ncd);
		} 
		catch (IOException e1) { e1.printStackTrace(); return; }
		
		Array lats = swath.getLats();
		Array lons = swath.getLons();


		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
//			Coordinate [] coords = new Coordinate[(int) lats.getSize()];

		List <Coordinate> swathCoords = new LinkedList <> ();
		List <LineString> swathLines = new LinkedList <> ();
		List <Coordinate> rasterCoords = new LinkedList <> ();
		List <LineString> rasterLines = new LinkedList <> ();
		int step = 10;
		Coordinate prevCoord = null;
		for(int idx = 0; idx < lats.getSize(); idx ++ )
		{
			float latitude = lats.getFloat(idx);
			float longitude = lons.getFloat(idx);
			Coordinate coord = new Coordinate(latitude, longitude);
			if( prevCoord != null && Math.abs(coord.x - prevCoord.x) > step)
			{
				LineString line = geometryFactory.createLineString( swathCoords.toArray(new Coordinate[swathCoords.size()]) );

				swathLines.add( line );
				
				swathCoords = new LinkedList <> ();
			}
			
			swathCoords.add( coord );
			
			if( idx >= cidx - span && idx < cidx + span)
			{
				if( prevCoord != null && Math.abs(coord.x - prevCoord.x) > step)
				{
					LineString line = geometryFactory.createLineString( rasterCoords.toArray(new Coordinate[rasterCoords.size()]) );
	
					rasterLines.add( line );
					
					rasterCoords = new LinkedList <> ();
				}
				rasterCoords.add( coord );
			}

			prevCoord = coord;
		}

		swathLines.add( geometryFactory.createLineString( swathCoords.toArray(new Coordinate[swathCoords.size()]) ) );
		rasterLines.add( geometryFactory.createLineString( rasterCoords.toArray(new Coordinate[rasterCoords.size()]) ) );

		DefaultFeatureCollection swathLine = new DefaultFeatureCollection();
		DefaultFeatureCollection rasterLine = new DefaultFeatureCollection();
		DefaultFeatureCollection arrowLine = new DefaultFeatureCollection();
		SimpleFeatureType type;
		try {
			type = DataUtilities.createType( "LINE", "centerline:MultiLineString:srid=4326");
		} catch (SchemaException e) {
			
			e.printStackTrace();
			return;
		}

		
		Coordinate [] arrowCoords = new Coordinate[3];
		List <LineString> arrowLines = new LinkedList <> ();
		
		Coordinate head = new Coordinate(lats.getFloat(cidx+span), lons.getFloat(cidx+span));
		Vector2 dir = new Vector2(lats.getFloat(cidx+span)-lats.getFloat(cidx+span-10), lons.getFloat(cidx+span)-lons.getFloat(cidx+span-10));  
		
		float dirAngle = (float) (dir.angle() * Math.PI  / 180);
		float leftAngle = (float) ((dir.angle()+15) * Math.PI  / 180);
		float rightAngle = (float) ((dir.angle()-15) * Math.PI  / 180);
		float arrowSize = 5;
		Coordinate leftArrow  = new Coordinate(head.x-arrowSize*(float)Math.cos(leftAngle), head.y-arrowSize*(float)Math.sin(leftAngle) );
		Coordinate rightArrow = new Coordinate(head.x-arrowSize*(float)Math.cos(rightAngle), head.y-arrowSize*(float)Math.sin(rightAngle));
		arrowCoords[0] = leftArrow;
		arrowCoords[1] = head;
		arrowCoords[2] = rightArrow;
		SimpleFeatureBuilder arrowFeatureBuilder = new SimpleFeatureBuilder( type );
		arrowFeatureBuilder.add( geometryFactory.createLineString( arrowCoords ) );

		SimpleFeature arrowFeature = arrowFeatureBuilder.buildFeature(null);
		arrowLine.add(arrowFeature);
	
		for( LineString line : swathLines )
		{ 
			SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder( type );
			featureBuilder.add( line );

			SimpleFeature feature = featureBuilder.buildFeature(null);
			swathLine.add(feature);
		}
		for( LineString line : rasterLines )
		{
			SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder( type );
			featureBuilder.add( line );

			SimpleFeature feature = featureBuilder.buildFeature(null);
			rasterLine.add(feature);
		}
		
		Color swathColor = Color.LIGHT_GRAY;
        Style swathStyle = MapRenderer.createLineStyle(1, swathColor);

		Layer swathLayer = new FeatureLayer(swathLine, swathStyle);

		renderer.getMap().addLayer(swathLayer);
		
		
		Color rasterColor = new Color(0xFEFD93);
	    Style rasterStyle = MapRenderer.createLineStyle(6, rasterColor);

		Layer rasterLayer = new FeatureLayer(rasterLine, rasterStyle);
		renderer.getMap().addLayer(rasterLayer);
		
		Color arrowColor = new Color(0xFEFD93);
	    Style arrowStyle = MapRenderer.createLineStyle(0, arrowColor);
		Layer arrowLayer = new FeatureLayer(arrowLine, arrowStyle);
		renderer.getMap().addLayer(arrowLayer);

		/////////////////////////////////////
		// render to canvas
		
		ReferencedEnvelope mapArea = renderer.getMap().getMaxBounds();
		BoundingBox box = new Envelope2D(
				null,
				new Rectangle2D.Double(REF_LON-30, REF_LAT-30, 60, 60));
		mapArea.setBounds(box);
 
		GTRenderer draw = new StreamingRenderer();
		draw.setMapContent(renderer.getMap());
		draw.paint(g2d, screenArea, mapArea );

		
		renderer.getMap().dispose();
	}
}
