package ubh.loader;

public enum LoadingState {
	/** Object was not loaded yet */
	NOT_LOADED,
	/** Object is currently in the process of being loaded.
	 *  (this value is used by ContentRegistry to detect cyclic dependencies)
	 */
	LOADING_IN_PROGRESS,
	/** Object has been succesfully loaded */
	LOADED,
	/** Loading of the object failed due to some sort of error */
	ERROR
}
