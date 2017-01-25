package meteo.icing.utils;

import java.lang.reflect.Type;

import com.badlogic.gdx.graphics.Color;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class LibGDXColorDeserializer implements JsonDeserializer <Color>
{

	@Override
	public Color deserialize( JsonElement element, Type arg1, JsonDeserializationContext arg2 ) throws JsonParseException
	{
		long hex = Long.parseLong(element.getAsString(), 16);
		Color color = new Color( (int)hex );
		return color;
	}

}
