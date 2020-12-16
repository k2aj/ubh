package ubh.loader;

import org.hjson.JsonValue;

@FunctionalInterface
public interface ContentLoader<T> {
	public T load(ContentRegistry registry, JsonValue data) throws ContentException;
}
