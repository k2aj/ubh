package ubh.entity;

import java.awt.Color;

import org.hjson.JsonValue;

import ubh.UBHGraphics;
import ubh.loader.ContentException;
import ubh.loader.ContentRegistry;
import ubh.math.ReferenceFrame;

public class Ship extends Living {
	
	protected Ship(Builder<?> builder) {
		super(builder);
	}
	public static Builder<?> builder() {
        return new Builder<>();
    }
    @SuppressWarnings("unchecked") 
    public static class Builder <This extends Builder<This>> extends Living.Builder<This> {
		@Override
		public Ship build() {
			return new Ship(this);
		}
    }
    
	@Override
	public Entity createEntity(ReferenceFrame referenceFrame, Affiliation affiliation) {
		return new Entity(referenceFrame, affiliation);
	}
	
    public class Entity extends Living.Entity {

		protected Entity(ReferenceFrame referenceFrame, Affiliation affiliation) {
			super(referenceFrame, affiliation);
		}
		@Override
		public void draw(UBHGraphics g) {
			g.setColor(affiliation == Affiliation.FRIENDLY ? Color.green : Color.red);
			g.enableFill();
			hitbox.draw(g);
		}
    }

    public static Ship fromJson(ContentRegistry registry, JsonValue json) throws ContentException {
    	var builder = builder();
		builder.loadJson(registry, json);
		return builder.build();
    }

}
