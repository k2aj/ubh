package ubh.loader;

public class ContentException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public ContentException(String message, Throwable t) {
		super(message, t);
	}
	
	public ContentException(String message) {
		super(message);
	}

}
