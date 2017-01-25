package meteo.geo;

import java.awt.Color;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.geotools.data.DataUtilities;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

import meteo.icing.utils.ColorScale;
import meteo.icing.utils.ColorScaleConf;
import ucar.ma2.Array;
import ucar.nc2.Group;
import ucar.nc2.dataset.NetcdfDataset;

public class SwathTest
{

    /**
     * GeoTools Quickstart demo application. Prompts the user for a shapefile and displays its
     * contents on the screen in a map frame
     */
    public static void main(String[] args) throws Exception
    {

    	MapRenderer renderer = new MapRenderer();


		ColorScaleConf colorConf = ColorScaleConf.read("E:/Development/workspaces/meteo/meteo.icing/data/etc/chromatic.colorscale");
		ColorScale scale = new ColorScale(0, 2*TestData.FILENAMES.length-1, colorConf);

		Color [] colors = new Color [] { Color.RED, Color.BLUE, Color.GREEN };


		int swathIdx = 0;
		for( String filename : TestData.FILENAMES )
		{

			File satfile = new File( filename );

			NetcdfDataset ncd = NetcdfDataset.openDataset( satfile.getAbsolutePath() );

			Group productRoot = ncd.getRootGroup().getGroups().get(0);

			ISwath swath = new GranuleSwath(ncd);
			Array lats = swath.getLats();
			Array lons = swath.getLons();


			GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
//			Coordinate [] coords = new Coordinate[(int) lats.getSize()];

			List <Coordinate> coords = new LinkedList <> ();
			List <LineString> lines = new LinkedList <> ();

			Coordinate prevCoord = null;
			for(int idx = 0; idx < lats.getSize(); idx ++ )
			{
				float latitude = lats.getFloat(idx);
				float longitude = lons.getFloat(idx);
				Coordinate coord = new Coordinate(longitude, latitude);
				if( prevCoord != null && Math.abs(coord.x - prevCoord.x) > 10)
				{
					LineString line = geometryFactory.createLineString( coords.toArray(new Coordinate[coords.size()]) );
					coords = new LinkedList <> ();

					lines.add( line );
				}
				coords.add( coord );

				prevCoord = coord;
			}

			lines.add( geometryFactory.createLineString( coords.toArray(new Coordinate[coords.size()]) ) );

			DefaultFeatureCollection lineCollection = new DefaultFeatureCollection();
			SimpleFeatureType type = DataUtilities.createType( "LINE",
					"centerline:MultiLineString:srid=4326");

			for( LineString line : lines )
			{
				SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder( type );
				featureBuilder.add( line );

				SimpleFeature feature = featureBuilder.buildFeature(null);
				lineCollection.add(feature);

			}

			Color swathColor = scale.toColor(swathIdx, scale.AWT_CREATOR);

	        Style swathStyle = MapRenderer.createLineStyle(2, swathColor);

			Layer swathLayer = new FeatureLayer(lineCollection, swathStyle);

			renderer.getMap().addLayer(swathLayer);

	        swathIdx ++;
		}
       // Now display the map
		JMapFrame frame = new JMapFrame(renderer.getMap());
        frame.enableStatusBar(true);
        frame.enableToolBar(true);
        frame.initComponents();
        frame.setSize(800, 600);
        frame.getMapPane().setBackground(Color.BLACK);
        frame.setVisible(true);
       // JMapFrame.showMap(map);

    }

}
