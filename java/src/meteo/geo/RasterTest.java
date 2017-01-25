package meteo.geo;

import java.awt.Color;
import java.io.File;

import meteo.icing.utils.BufferedCanvas;
import meteo.icing.utils.ColorScale;
import meteo.icing.utils.ColorScaleConf;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

public class RasterTest
{
	public static void main( String[] args ) throws Exception
	{
		///////////////////////////////////////////////////////////
		String[] filenames = new String[] {
				"E:/Development/workspaces/meteo/meteo.icing/data/satellite/2010025005412_19917_CS_2B-GEOPROF-LIDAR_GRANULE_P2_R04_E03.hdf",
//				"E:/Development/workspaces/meteo/meteo.icing/data/satellite/2010025023305_19918_CS_2C-ICE_GRANULE_P1_R04_E03.hdf",
//				"E:/Development/workspaces/meteo/meteo.icing/data/satellite/2010025041158_19919_CS_2C-ICE_GRANULE_P1_R04_E03.hdf",
//				"E:/Development/workspaces/meteo/meteo.icing/data/satellite/2010025072944_19921_CS_2C-ICE_GRANULE_P1_R04_E03.hdf",
//				"E:/Development/workspaces/meteo/meteo.icing/data/satellite/2010025090837_19922_CS_2C-ICE_GRANULE_P1_R04_E03.hdf",
//				"E:/Development/workspaces/meteo/meteo.icing/data/satellite/2010025104730_19923_CS_2C-ICE_GRANULE_P1_R04_E03.hdf",
//				"E:/Development/workspaces/meteo/meteo.icing/data/satellite/2010025122623_19924_CS_2C-ICE_GRANULE_P1_R04_E03.hdf",
//				"E:/Development/workspaces/meteo/meteo.icing/data/satellite/2010025140516_19925_CS_2C-ICE_GRANULE_P1_R04_E03.hdf",
//				"E:/Development/workspaces/meteo/meteo.icing/data/satellite/2010025154409_19926_CS_2C-ICE_GRANULE_P1_R04_E03.hdf",
//				"E:/Development/workspaces/meteo/meteo.icing/data/satellite/2010025172302_19927_CS_2C-ICE_GRANULE_P1_R04_E03.hdf",
//				"E:/Development/workspaces/meteo/meteo.icing/data/satellite/2010025190155_19928_CS_2C-ICE_GRANULE_P1_R04_E03.hdf",
//				"E:/Development/workspaces/meteo/meteo.icing/data/satellite/2010025204048_19929_CS_2C-ICE_GRANULE_P1_R04_E03.hdf",
//				"E:/Development/workspaces/meteo/meteo.icing/data/satellite/2010025221941_19930_CS_2C-ICE_GRANULE_P1_R04_E03.hdf",
//				"E:/Development/workspaces/meteo/meteo.icing/data/satellite/2010025235834_19931_CS_2C-ICE_GRANULE_P1_R04_E03.hdf",

		};

		String parameter = "TAB_simulation";

		ColorScaleConf colorConf = ColorScaleConf.read("E:/Development/workspaces/meteo/meteo.icing/data/etc/chromatic.colorscale");
		ColorScale scale = new ColorScale(0, 1, colorConf);

		int swathIdx = 0;
		for( String filename : filenames )
		{

			File satfile = new File(filename);

			NetcdfDataset ncd = NetcdfDataset.openDataset(satfile.getAbsolutePath());
			Group productRoot = ncd.getRootGroup().getGroups().get(0);


			ISwath swath = new GranuleSwath( ncd );
			Array lats = swath.getLats();
			Array lons = swath.getLons();

			Group dataGroup = productRoot.findGroup("Data_Fields");

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

			Variable iceWaterVar = dataGroup.findVariable(parameter);

			// Array iceWaterData = iceWaterVar.read(new int [] {0, 0},
			// iceWaterVar.getShape());
			ArrayFloat.D2 iceWaterData = (ArrayFloat.D2) iceWaterVar.read();
			// iceWaterData.

			int w = iceWaterData.getShape()[0];
			int h = iceWaterData.getShape()[1];

			BufferedCanvas canvas = new BufferedCanvas( w, h );
			float min = Float.MAX_VALUE;
			float max = Float.MIN_VALUE;
			for(int x = 0; x < w; x ++)
				for(int y = 0; y < h; y ++)
				{
					float value = iceWaterData.get(x, y);
					if( value > max ) max = value;
					if( value < min ) min = value;
				}
			scale = new ColorScale(min, max, colorConf);


			for(int x = 0; x < w; x ++)
				for(int y = 0; y < h; y ++)
				{
					float value = iceWaterData.get(x, y);
					Color color = scale.toAWTColor(value);
					if( color != null)
					{
						canvas.g2d.setColor(scale.toAWTColor(value));
						canvas.g2d.drawLine(x, y, x, y);
					}
					else
					{
						canvas.g2d.setColor(Color.RED);
						canvas.g2d.drawLine(x, y, x, y);
					}
				}

			canvas.writeImage(parameter + "_test.png");

		}
	}
}
