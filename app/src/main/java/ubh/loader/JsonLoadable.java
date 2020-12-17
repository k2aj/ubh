package ubh.loader;

import org.hjson.JsonValue;

public interface JsonLoadable {
	public void loadFieldFromJson(String field, ContentRegistry registry, JsonValue json) throws ContentException;
	default public void loadJson(ContentRegistry registry, JsonValue json) throws ContentException {
		for(var member : json.asObject())
			loadFieldFromJson(member.getName(), registry, member.getValue());
	}
}
