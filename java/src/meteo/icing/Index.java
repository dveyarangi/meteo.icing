package meteo.icing;

import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.utils.LongMap;

public class Index 
{

	private LongMap<List<String>> eraIndex;
	
	private LongMap<List<String>> satIndex;
	
	public Index() 
	{
		this.eraIndex = new LongMap <> ();
		this.satIndex = new LongMap <> ();
		
	}
}
