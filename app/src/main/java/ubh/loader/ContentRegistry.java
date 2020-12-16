package ubh.loader;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.hjson.JsonValue;

import ubh.attack.*;
import ubh.entity.*;
import ubh.math.*;

public final class ContentRegistry {
	
	private Map<String, ContentLoader<?>> loaders = new HashMap<>();
	private Map<Class<?>, ContentLoader<?>> defaultLoaders = new HashMap<>();
	private Map<Class<?>, Object> defaultValues = new HashMap<>();
	
	public void registerLoader(String name, ContentLoader<?> loader) {
		loaders.put(name, loader);
	}
	public <T> void registerLoader(Class<T> clazz, ContentLoader<T> loader) {
		defaultLoaders.put(clazz, loader);
	}
	public <T> void registerDefault(Class<T> clazz, T value) {
		defaultValues.put(clazz, value);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T load(Class<T> clazz, JsonValue data) throws ContentException {
		
		Object result = null;
		
		// (H)Json objects should have a "type" property describing which loader to use
		if(data.isObject()) {
			final var obj = data.asObject();
			final var type = obj.getString("type", null);
			if(type != null) {
				final var loader = loaders.get(type);
				if(loader != null) {
					try {result = loader.load(this, data);}
					catch (Exception e) {
						// TODO: THIS IS HORRIBLE
						// REPLACE IT WITH PROPER EXCEPTION HANDLING LATER
						e.printStackTrace();
					}
					if(!clazz.isInstance(result))
						result = null;
				}
			}
		}
		// Try to use default loader for specified type
		if(result == null) {
			final var loader = defaultLoaders.get(clazz);
			if(loader != null)
				try {result = loader.load(this, data);}
				catch (Exception e) {
					// TODO: THIS IS HORRIBLE
					// REPLACE IT WITH PROPER EXCEPTION HANDLING LATER
					e.printStackTrace();
				}
		}
		// Try to return the default object for that type
		if(result == null)
			result = defaultValues.get(clazz);
		
		if(result != null)
			return (T) result;
		else
			throw new ContentException("Failed to load content.");
	}
	
	public <T> T loadFromResource(Class<T> loadedClass, Class<?> c, String resourceName) {
		try(var reader = new InputStreamReader(c.getResourceAsStream(resourceName))) {
			return load(loadedClass, JsonValue.readHjson(reader));
		} catch (IOException e) {
			throw new Error(e);
		}
	}
	
	public static ContentRegistry createDefault() {
		var registry = new ContentRegistry();
		
		registry.registerLoader(Color.class, (reg,json) -> {
			if(json.isObject()) {
				boolean isHsv = false;
				float r=0,g=0,b=0,h=0,s=0,v=0,a=1;
				for(var member : json.asObject()) {
					switch(member.getName()) {
					case "r": case "red": r = member.getValue().asFloat(); break;
					case "g": case "green": g = member.getValue().asFloat(); break;
					case "b": case "blue":  b = member.getValue().asFloat(); break;
					case "a": case "alpha": a = member.getValue().asFloat(); break;
					case "h": case "hue": isHsv = true; h = member.getValue().asFloat(); break;
					case "s": case "saturation": isHsv = true; s = member.getValue().asFloat(); break;
					case "v": case "value": isHsv = true; v = member.getValue().asFloat(); break;
					}
				}
				if(isHsv) {
					int rgba = Color.HSBtoRGB(h,s,v);
					rgba |= Math.round(a*255) << 24;
					return new Color(rgba);
				} else {
					return new Color(r,g,b,a);
				}
			} else if(json.isString()) {
				switch(json.asString().toLowerCase()) {
				case "red": return Color.red;
				case "green": return Color.green;
				case "blue": return Color.blue;
				case "cyan": return Color.cyan;
				case "magenta": return Color.magenta;
				case "yellow": return Color.yellow;
				case "black": return Color.black;
				case "white": return Color.white;
				default: throw new ContentException("Unknown color: "+json.asString());
				}
			} else {
				throw new ContentException(json.toString() + " is not a valid Color");
			}
		});
		registry.registerLoader(Vector2.class, Vector2::fromJson);
		registry.registerLoader(Shape.class, Shape::fromJson);
		registry.registerLoader("Circle", Circle::fromJson);
		registry.registerLoader("Rectangle", Rectangle::fromJson);
		
		registry.registerDefault(Attack.class, Attack.NULL);
		registry.registerLoader("Bullet", Bullet::fromJson);
		registry.registerLoader("Ship", Ship::fromJson);
		
		return registry;
	}
}
