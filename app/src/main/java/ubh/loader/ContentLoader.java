package ubh.loader;

import org.hjson.JsonValue;

/** {@code ContentLoader} is responsible for loading a single type of content from a JSON description.
 * <p>
 * 
 *
 * @param <T> Type of the loaded content.
 */
@FunctionalInterface
public interface ContentLoader<T> {
	/** Loads a piece of content.
	 * @implNote
	 * {@code load()} should not perform type checking / error checking on the passed {@code json}; it is ok if {@code load()}
	 * fails with an exception. It is the responsibility of the {@link ContentRegistry} using the loader to recover from the error.
	 * @param registry Registry used to load dependencies for the content.
	 * @param json Describes the content.
	 * @return The loaded content.
	 * @throws ContentException When loading the content fails. 
	 */
	public T load(ContentRegistry registry, JsonValue json) throws ContentException;
}
