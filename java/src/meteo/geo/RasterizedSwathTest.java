package meteo.geo;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;

import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;

import meteo.icing.utils.BufferedCanvas;
import meteo.icing.utils.ColorScale;
import meteo.icing.utils.ColorScaleConf;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.nc2.dataset.NetcdfDataset;

public class RasterizedSwathTest
{

	static int WIDTH = 2400;
	static int HEIGHT = 1600;

	static String PARAM = "IWC";

    public static void main(String[] args) throws Exception
    {

    	MapRenderer renderer = new MapRenderer();


		ColorScaleConf colorConf = ColorScaleConf.read("E:/Development/workspaces/meteo/meteo.icing/data/etc/chromatic.colorscale");

		ColorScale scale = new ColorScale(0, 1, colorConf);

		BufferedCanvas canvas = new BufferedCanvas( WIDTH, HEIGHT );

        Rectangle rect = new Rectangle(0, 0, WIDTH, HEIGHT);

		GTRenderer draw = new StreamingRenderer();
		draw.setMapContent(renderer.getMap());

		draw.paint( canvas.g2d, rect, renderer.getMap().getMaxBounds());

		renderer.getMap().dispose();

		for( String filename : TestData.FILENAMES )
//	String filename = TestData.FILENAMES[0];
		{

			File satfile = new File( filename );

			NetcdfDataset ncd = NetcdfDataset.openDataset( satfile.getAbsolutePath() );

			Granule granule = new Granule( ncd );

			ArrayFloat.D2 values = granule.read(PARAM);

			int trackLength = values.getShape()[0];
			int trackWidth = values.getShape()[1];

			float min = Float.MAX_VALUE;
			float max = Float.MIN_VALUE;
			for(int x = 0; x < trackLength; x ++)
				for(int y = 0; y < trackWidth; y ++)
				{
					float value = values.get(x, y);
					if( value > max ) max = value;
					if( value < min ) min = value;
				}
			scale = new ColorScale(min, max, colorConf);


			Array lats = granule.swath.getLats();
			Array lons = granule.swath.getLons();

			float binWidth = 0.02f;
			float crossTrackLon = binWidth * trackWidth;

			for(int alongTrackIdx = 0; alongTrackIdx < trackLength; alongTrackIdx ++ )
			{
				float latitude = lats.getFloat( alongTrackIdx );
				float longitude = lons.getFloat( alongTrackIdx );

				float x = toX( longitude );
				float y = toY( latitude );

				//Array bins = values.slice(0, alongTrackIdx);

				//canvas.g2d.setColor( Color.RED );
				//canvas.g2d.drawLine((int)x, (int)y, (int)x, (int)y);

				for(int binIdx = 0; binIdx < trackWidth; binIdx ++)
				{
					//float value = bins.getFloat( binIdx );
					float value = values.get(alongTrackIdx, binIdx);

					Color color = scale.toAWTColor( value );

					if( color == null)
						continue;

					canvas.g2d.setColor( color );
//					int px = (int)(x + binIdx -	trackWidth/2);
//					int py = (int)(y);
					int px = (int)toX( longitude + binWidth *binIdx - crossTrackLon/2 );
					int py = (int)(y);

					canvas.g2d.drawLine(px, py, px, py);

				}

			}
		}


		canvas.writeImage(PARAM + "_day.png");
    }

    public static float toX(float lon)
    {
    	return WIDTH/2f + lon * (WIDTH/360f);
    }
    public static float toY(float lat)
    {
    	return HEIGHT/2f - lat * (HEIGHT/180f);
    }

}
