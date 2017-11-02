package meteo.icing.utils;

import java.lang.reflect.Type;

import com.badlogic.gdx.graphics.Color;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class LibGDXColorDeserializer implements JsonDeserializer <Color>, JsonSerializer<Color>
{

	@Override
	public Color deserialize( JsonElement element, Type arg1, JsonDeserializationContext arg2 ) throws JsonParseException
	{
		long hex = Long.parseLong( element.getAsString(), 16 );
		Color color = new Color( (int)hex );
		return color;
	}

	@Override
	public JsonElement serialize( Color src, Type typeOfSrc, JsonSerializationContext context )
	{
		String colorstr = src.toString();
		return new JsonPrimitive( colorstr );
	}

}
