package meteo.icing.wrf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.io.Files;

import lombok.extern.slf4j.Slf4j;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

@Slf4j
public class WRFNetCDFSplitter 
{
	public static final String dataRoot = "E:/meteo/dropbox/Dropbox/Icing/WRF-output-3X3";
	public static final String outRoot = "E:/meteo/test/Icing/WRF-output-3X3-split";

	public static Map <String, String> DIMMAP = new HashMap <> ();
	static {
		DIMMAP.put("Time", "Times");
		DIMMAP.put("DateStrLen","");
		DIMMAP.put("west_east","XLON");
		DIMMAP.put("south_north","XLAT");
		DIMMAP.put("bottom_top","");
		DIMMAP.put("bottom_top_stag","");
		DIMMAP.put("soil_layers_stag","");
		DIMMAP.put("west_east_stag","");
		DIMMAP.put("south_north_stag","");
	}
	
	public static void main( String ... args ) throws IOException
	{
		String dateDir = "2016112000";
		String filename = "wrfout_d01_2016-11-19_18_00_00";
		String filePath = dataRoot + File.separator + dateDir + File.separator + filename;
		NetcdfFile ncfile = null;
		try {
			ncfile = NetcdfFile.open(filePath);
			split( ncfile, dateDir, filename );
		} catch (IOException ioe) {
			log.error("trying to open " + filename, ioe);
			return;
		} finally { 
			if (null != ncfile) try { ncfile.close(); } catch (IOException ioe) {	log.error("trying to close " + filename, ioe); }
		}
		
		System.out.println("=== ");
		
	}
	
	public static final String [] AXIS_NAMES = { "Times", "DN" };

	private static void split(NetcdfFile ncfile, String dateDir, String filename) throws IOException 
	{
		// extracting axis variables:
		List <Variable> axes = new ArrayList <> ();
		for(Variable var : ncfile.getVariables())
		{
			for(String axisName :AXIS_NAMES )
				if( axisName.equals(var.getShortName()))
					axes.add( var );
		}
		
		for(Variable var : ncfile.getVariables())
		{
			if( var.getDimensions().size() != 4) // take only 4d vars
				continue;
			
			boolean isIsobaric = false;
			for( Dimension dim : var.getDimensions())
			{
				if( dim.getShortName().equals("bottom_top") )
				{
					isIsobaric = true;
					break;
				}
			}
			
			if( !isIsobaric )
				continue;
			
			String [] coordNames = getCoordNames( var );
			if( coordNames == null ) // skip dimensionless grids
				continue;
			
			String name = var.getShortName();
			
			String outPath = outRoot + File.separator + dateDir + File.separator + filename + "_" + name;
			
			Files.createParentDirs( new File(outPath) );
		
			NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4, outPath, null);
			
			List <Variable> varsToCopy = new ArrayList <> ();
			
			varsToCopy.add( var );
			
			for(Variable axis : axes)
				varsToCopy.add( axis );
			
			for(Variable aVar : ncfile.getVariables())
			{
				for(String coordName : coordNames)
					if( coordName.equals(aVar.getShortName()))
						varsToCopy.add( aVar );
			}
			
			Map <Dimension, Dimension> registeredDims = new HashMap <> ();
			List <Variable> copiedVars = new ArrayList <> ();
			for(Variable varToCopy : varsToCopy)
			{
				Variable copiedVar = copyVar(writer, varToCopy, registeredDims);
				copiedVars.add( copiedVar );
			}
			
			////////////////////////////////////////
			// write headers
			writer.create();
			
			
			try {
				for(int vidx = 0; vidx < varsToCopy.size(); vidx ++)
				{
					Variable oldVar = varsToCopy.get(vidx);
					Variable newVar = copiedVars.get(vidx);
					writer.write(newVar, oldVar.read());
				}
			
			} catch (InvalidRangeException e) {
				e.printStackTrace();
			}
			writer.close();
		}
	}

	private static Variable copyVar(NetcdfFileWriter writer, Variable var, Map <Dimension, Dimension> createdDims) 
	{
		List <Dimension> dims = new ArrayList <> ();
		
		for( Dimension dim : var.getDimensions() )
		{
			Dimension newDim = null;
			if( createdDims.containsKey( dim ))
				newDim = createdDims.get( dim );
			else
			{
				newDim = writer.addDimension(null,  dim.getShortName(), dim.getLength());
				createdDims.put( dim, newDim );
			}
			dims.add( newDim );
			
			
		}
		
		Variable newVar = writer.addVariable(null, var.getShortName(), var.getDataType(), dims);
		
		
		
		for(Attribute att : var.getAttributes())
		{
			writer.addVariableAttribute(newVar, att);
		}
		
		return newVar;
	}

	private static String[] getCoordNames(Variable var) 
	{
		Attribute coordAtt = null;
		for(Attribute att : var.getAttributes())
		{
			if( "coordinates".equals(att.getShortName()) )
			{
				coordAtt = att;
				break;
			}
		}
		if( coordAtt == null )
			return null;
		
		String [] coords = coordAtt.getStringValue().split(" ");
		
		return coords;
	}
}
