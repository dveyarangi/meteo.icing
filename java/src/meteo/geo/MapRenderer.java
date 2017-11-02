package meteo.geo;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.StrokeImpl;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.opengis.filter.FilterFactory;
import org.opengis.geometry.BoundingBox;

public class MapRenderer
{
	MapContent map;

	public MapRenderer()
	{
        ///////////////////////////////////////////////////////////
        // display a data store file chooser dialog for shapefiles
        File file = new File( "E:/Development/workspaces/meteo/meteo.icing/etc/maps/ne_50m_admin_0_countries_lakes.shp" );


        SimpleFeatureSource featureSource = null;
		try
		{
			FileDataStore store = FileDataStoreFinder.getDataStore(file);

			featureSource = store.getFeatureSource();

		} catch( IOException e )
		{
			e.printStackTrace();
		}
        // Create a map content and add our shapefile to it
        map = new MapContent();
        map.setTitle("Swathing");
        
        double latSpan = 90;
        double lonSpan = 180;
        double latCenter = 0;
        double lonCenter = 0;


		ReferencedEnvelope mapArea = map.getMaxBounds();
		BoundingBox box = new Envelope2D(
				null,
				new Rectangle2D.Double((lonCenter-lonSpan/2), (latCenter-latSpan/2), lonSpan, latSpan));
		mapArea.setBounds(box);

        Style style = createLineStyle(1, Color.LIGHT_GRAY );
        Layer layer = new FeatureLayer(featureSource, style);
        map.addLayer(layer);
	}


	public static Style createLineStyle(int borderWidth, Color borderColor )
	{
		StyleBuilder styleBuilder = new StyleBuilder();
		Style style = styleBuilder.createStyle();



		//        PointSymbolizer pointSymbolizer = styleBuilder.createPointSymbolizer();
		//        Graphic graphic = styleBuilder.createGraphic();
		//       ExternalGraphic external = styleBuilder.createExternalGraphic("file:///C:/images/house.gif",
		//                "image/gif");
		//        graphic.graphicalSymbols().add(external);
		//        graphic.graphicalSymbols().add(styleBuilder.createMark("circle"));

		//        pointSymbolizer.setGraphic(graphic);
		final FilterFactory ff = CommonFactoryFinder.getFilterFactory();

		LineSymbolizer lineSymbolizer = styleBuilder.createLineSymbolizer();

		Stroke stroke = new StrokeImpl() {};
		stroke.setWidth(ff.literal(borderWidth));
		stroke.setColor(ff.literal(borderColor));

		lineSymbolizer.setStroke(stroke);
		Rule rule = styleBuilder.createRule(lineSymbolizer);
		FeatureTypeStyle featureTypeStyle = styleBuilder.createFeatureTypeStyle("Feature", rule);
		style.featureTypeStyles().add(featureTypeStyle);
		return style;
	}


	public MapContent getMap() { return map; }

/*	public static Style createPointStyle(int borderWidth, Color borderColor )
	{
		StyleBuilder styleBuilder = new StyleBuilder();
		Style style = styleBuilder.createStyle();


		final FilterFactory ff = CommonFactoryFinder.getFilterFactory();

		PointSymbolizer pointSymbolizer = styleBuilder.createPointSymbolizer();
		Stroke stroke = new StrokeImpl() {};
		stroke.setWidth(ff.literal(borderWidth));
		stroke.setColor(ff.literal(borderColor));
		pointSymbolizer.setGraphic(graphic);
		Graphic graphic = styleBuilder.createGraphic();
		ExternalGraphic external = styleBuilder.createExternalGraphic("file:///C:/images/house.gif", "image/gif");
		//        graphic.graphicalSymbols().add(external);
		//        graphic.graphicalSymbols().add(styleBuilder.createMark("circle"));

		//        pointSymbolizer.setGraphic(graphic);

		Rule rule = styleBuilder.createRule(pointSymbolizer);
		FeatureTypeStyle featureTypeStyle = styleBuilder.createFeatureTypeStyle("Feature", rule);
		style.featureTypeStyles().add(featureTypeStyle);
		return style;
	}*/

}
