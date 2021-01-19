package ubh.loader;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.hjson.JsonValue;

import ubh.attack.*;
import ubh.entity.*;
import ubh.entity.ai.*;
import ubh.math.*;

/** {@code ContentRegistry} is responsible for loading various types of game content. */
public final class ContentRegistry {	
	
	private ContentRegistry() {}
	
	private Map<String, ContentLoader<?>> namedLoaders = new HashMap<>();
	private Map<Class<?>, ContentLoader<?>> defaultLoaders = new HashMap<>();
	private Map<Class<?>, Object> defaultValues = new HashMap<>();
	
	private Map<String, JsonValue> sourceIndex = new HashMap<>();
	private Map<String, Object> objectIndex = new HashMap<>();
	private Map<String, LoadingState> objectLoadingStates = new HashMap<>();
	
	/** Recursively finds all objects within json that have a string-valued id field and adds them to sourceIndex */
	private void createIndexEntriesFor(JsonValue json) {
		if(json.isObject()) {
			for(var member : json.asObject()) {
				if(member.getName().equals("id") && member.getValue().isString()) {
					var id = member.getValue().asString();
					if(sourceIndex.containsKey(id)) {
						// TODO: do something sensible when getting duplicate content IDs
						// currently we just skip future objects with the same id
					} else {
						sourceIndex.put(id, json);
						objectLoadingStates.put(id, LoadingState.NOT_LOADED);
					}
				} else {
					createIndexEntriesFor(member.getValue());
				}
			}
		} else if(json.isArray()) {
			for(var element : json.asArray())
				createIndexEntriesFor(element);
		}
	}
	
	public void addHjsonSource(Class<?> clazz, String resource) {
		try(var reader = new BufferedReader(new InputStreamReader(clazz.getResourceAsStream(resource)))) {
			createIndexEntriesFor(JsonValue.readHjson(reader));
		} catch (IOException e) {
			throw new RuntimeException(e);
			// TODO: handle error
		}
	}
	
	public void addHjsonSource(String path) {
		try(var reader = new BufferedReader(new FileReader(path))) {
			createIndexEntriesFor(JsonValue.readHjson(reader));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
			/// TODO: handle error
		} catch (IOException e) {
			throw new RuntimeException(e);
			/// TODO: handle error
		}
	}
	
	
	
	/** Registers a named content loader.
	 * <p>
	 *  This loader will be dispatched on {@code JsonObject} instances with a {@code type}
	 *  property matching {@code name} used to register the loader.
	 * @param name 
	 * @param loader
	 * @see ContentRegistry#load
	 */
	public void registerLoader(String name, ContentLoader<?> loader) {
		namedLoaders.put(name, loader);
	}
	/** Registers a default content loader for a content type. 
	 *  <p>
	 *  Default content loader serves as a fallback when named loader does not exist, fails, or can not be found
	 *  due to missing {@code type} property on the {@code JsonValue}.
	 *  <p>
	 *  Default content loader is dispatched based on the expected content type.
	 * @param <T> Type of the loaded content.
	 * @param clazz Should be {@code T.class}, this is needed because Java uses type erasure for generics. 
	 * @param loader
	 * @see ContentRegistry#load
	 */
	public <T> void registerLoader(Class<T> clazz, ContentLoader<T> loader) {
		defaultLoaders.put(clazz, loader);
	}
	/** Registers a default value for a content type.
	 *  <p>
	 *  The default value serves as a final fallback when both named and default loader for the content are missing or fail.
	 *  
	 * @param <T> Type of the content.
	 * @param clazz Should be {@code T.class}, this is needed because Java uses type erasure for generics. 
	 * @param value The default value for the content type. 
	 * <p> 
	 * Default value should be safe to use as a replacement for loaded content;
	 * it may miss functionality, but must not crash the program or cause unexpected behavior (for this reason {@code null} and NaN are
	 * not valid default values).
	 * <p>
	 * Default value should be immutable, as it may be shared by multiple objects.
	 */
	public <T> void registerDefault(Class<T> clazz, T value) {
		defaultValues.put(clazz, value);
	}
	
	public void register(String id, Object value) {
		objectIndex.put(id, value);
		objectLoadingStates.put(id, LoadingState.LOADED);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T load(Class<T> clazz, String id) {
		switch(objectLoadingStates.get(id)) {
		case LOADED: return (T) objectIndex.get(id);
		case NOT_LOADED:
			objectLoadingStates.put(id, LoadingState.LOADING_IN_PROGRESS);
			try {
				var result = load(clazz, sourceIndex.get(id));
				register(id, result);
				return result;
			} catch (Exception e) {
				objectLoadingStates.put(id, LoadingState.ERROR);
				throw e;
			}
		case LOADING_IN_PROGRESS: throw new ContentException("Cyclic dependency");
		case ERROR: throw new ContentException(String.format("Object %s is invalid", id));
		default: throw new Error("This should never happen");
		}
	}
	
	/** Loads content from passed JsonValue.
	 *  <p>
	 *  If {@code JsonValue} has a {@code type} property, then its value is used to dispatch a named content loader.
	 *  <p>
	 *  If there is no {@code type} property, or the named loader is missing, or it fails, then loading is attempted using
	 *  the default loader for the expected content type ({@code T})
	 *  <p>
	 *  If both of the above fail, then the registered default value for the expected content type is returned.
	 * 
	 * @param <T> Expected type of the loaded content.
	 * @param clazz Should be {@code T.class}, this is needed because Java uses type erasure for generics. 
	 * @param json JsonValue describing the loaded content.
	 * @return The loaded content.
	 * @throws ContentException If all possible loaders fail and no default value is registered for the content type.
	 */
	@SuppressWarnings("unchecked")
	public <T> T load(Class<T> clazz, JsonValue json) throws ContentException {
		
		Object result = null;
		
		// If we got an unexpected string, then it's probably a named object id
		if(json.isString() && clazz != String.class) {
			var id = json.asString();
			if(objectLoadingStates.containsKey(id))
				return load(clazz, id);
		}
		
		// Try to use named loader
		if(json.isObject()) {
			final var obj = json.asObject();
			final var type = obj.getString("type", null);
			if(type != null) {
				final var loader = namedLoaders.get(type);
				if(loader != null) {
					try {result = loader.load(this, json);}
					catch (Exception e) {
						// TODO: THIS IS HORRIBLE
						// REPLACE IT WITH PROPER EXCEPTION HANDLING LATER
						throw new RuntimeException(e);
					}
					if(!clazz.isInstance(result))
						result = null;
				}
			}
		}
		// Try to use default loader
		if(result == null) {
			final var loader = defaultLoaders.get(clazz);
			if(loader != null)
				try {result = loader.load(this, json);}
				catch (Exception e) {
					// TODO: THIS IS HORRIBLE
					// REPLACE IT WITH PROPER EXCEPTION HANDLING LATER
					throw new RuntimeException(e);
				}
		}
		// Try to return the default object for that type
		if(result == null)
			result = defaultValues.get(clazz);
		
		if(result != null)
			return (T) result;
		else
			// Everything failed, bail out
			throw new ContentException("Failed to load content.");
	}
	
	/** Loads content from a Hjson resource file. 
	 * @param <T> Expected type of the loaded content.
	 * @param clazz Should be {@code T.class}, this is needed because Java uses type erasure for generics. 
	 * @param relativeTo Class used to load the resource file.
	 * @param resourceName Name of the resource file.
	 * @return The loaded content.
	 */
	public <T> T loadFromResource(Class<T> clazz, Class<?> relativeTo, String resourceName) {
		try(var reader = new InputStreamReader(relativeTo.getResourceAsStream(resourceName))) {
			return load(clazz, JsonValue.readHjson(reader));
		} catch (IOException e) {
			throw new Error(e);
		}
	}
	
	/** Creates a default {@code ContentRegistry} for UBH.
	 *  This is where loaders and default values should be registered.
	 */
	public static ContentRegistry createDefault() {
		var registry = new ContentRegistry();
		
		// JVM classes
		registry.registerLoader(Color.class, (reg,json) -> {
			if(json.isObject()) {
				// Try to handle both RGB and HSV colors
				boolean isHsv = false;
				float r=0,g=0,b=0,h=0,s=1,v=1,a=1;
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
			} else {
				throw new ContentException(json.toString() + " is not a valid Color");
			}
		});
		registry.register("red", Color.red);
		registry.register("green", Color.green);
		registry.register("blue", Color.blue);
		registry.register("cyan", Color.cyan);
		registry.register("magenta", Color.magenta);
		registry.register("yellow", Color.yellow);
		registry.register("black", Color.black);
		registry.register("white", Color.white);
		
		// ubh.math
		registry.registerLoader(Vector2.class, Vector2::fromJson);
		registry.registerLoader(Shape.class, Shape::fromJson);
		registry.registerLoader("Circle", Circle::fromJson);
		registry.registerLoader("Rectangle", Rectangle::fromJson);
		
		// ubh.attack
		registry.registerDefault(Attack.class, Attack.NULL);
		registry.registerLoader(Attack.class, MultiAttack::fromJsonArray);
		registry.registerLoader("Bullet", Bullet::fromJson);
		registry.registerLoader("Explosion", Explosion::fromJson);
		registry.registerLoader("Beam", Beam::fromJson);
		registry.registerLoader("SpreadAttack", SpreadAttack::fromJson);
		registry.registerLoader("MultiAttack", MultiAttack::fromJson);
		
		registry.registerLoader(Weapon.class, Weapon::fromJson);
		
		// ubh.entity
		registry.registerLoader("Ship", Ship::fromJson);
		registry.registerDefault(AI.class, AI.NULL);
		registry.register("boss", BossAI.getInstance());
		
		return registry;
	}
}
