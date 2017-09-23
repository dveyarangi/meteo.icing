package meteo.icing.index;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.badlogic.gdx.utils.LongMap;

import meteo.icing.era.Conf;
import meteo.icing.era.DataStamp;

public class EraIndex 
{
	private Conf conf;
	
	private LongMap <List<String>> indexMap;
	
	public EraIndex( Conf conf)
	{
		this.conf = conf;
		
		this.indexMap = readIndex(); 
	}

	
	private LongMap<List<String>> readIndex() 
	{
		LongMap<List<String>> index = new LongMap <> ();
		
		File rootDir = new File( conf.eraDir ); 
		
		for(File monthdir : rootDir.listFiles())
		{
			if( ! monthdir.isDirectory() )
				continue;
			
			for( File daydir : monthdir.listFiles())
			{
				if( ! daydir.isDirectory() )
					continue;
				
				for( String filename : daydir.list())
				{
					String datePart = filename.substring(0, 13);
					DateTime datetime = DataStamp.eraFormat.parseDateTime( datePart );
					long timestamp = datetime.getMillis();
					List <String> files = index.get( timestamp );
					
					if( files == null )
					{
						files = new ArrayList <> ();
						index.put( timestamp, files );
					}
					
					files.add( monthdir.getName() + "/" + daydir.getName() + "/" + filename );
				}
			}
		}
		
		
		return index;
	}
	
	
	public LongMap<List<String>> get() { return indexMap; }
}
