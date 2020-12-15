package ubh;

import ubh.entity.Affiliation;
import ubh.entity.Living;
import ubh.math.Shape;
import java.util.EnumMap;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Random;

public final class CollisionSystem {
    private EnumMap<Affiliation, ArrayList<EntityEntry>> passiveColliders = new EnumMap<>(Affiliation.class);
    private EnumMap<Affiliation, ArrayList<ColliderEntry>> activeColliders = new EnumMap<>(Affiliation.class);
    private final Random random = new Random();

    private static final float DECAY_COOLDOWN = 1/60f;
    private float decayCooldownLeft = 0;
    long currentDecay = 0;

    public void update(float deltaT) {
        removeDeadColliders();
        tryDecay(deltaT);
    }
    
    public void runCollisions(Battlefield battlefield) {
    	runCollisions(battlefield, Affiliation.FRIENDLY, Affiliation.ENEMY);
        runCollisions(battlefield, Affiliation.ENEMY, Affiliation.FRIENDLY);
    }

    private void tryDecay(float deltaT) {
        while(decayCooldownLeft < deltaT) {
            decayCooldownLeft += DECAY_COOLDOWN;
            for(var list : activeColliders.values())
                for(var collider : list)
                    collider.decay(currentDecay);
            ++currentDecay;
            if(currentDecay == 64)
                currentDecay = 0;
        }
        decayCooldownLeft = Math.max(decayCooldownLeft-deltaT, 0);
    }

    private void runCollisions(Battlefield battlefield, Affiliation activeGroup, Affiliation passiveGroup) {
        var actives = activeColliders.get(activeGroup);
        var passives = passiveColliders.get(passiveGroup);
        if(passives != null && actives != null)
            for(var active : actives)
                for(var passive : passives)
                    active.tryCollideWith(battlefield, passive);
    }

    private void removeDeadColliders() {
        for(var list : activeColliders.values())
            list.removeIf(c -> c.dead);
        for(var list : passiveColliders.values())
            list.removeIf(c -> c.dead);
    }

    public AutoCloseable registerCollider(Collider collider, Shape hitbox, Affiliation group) {
        var entry = new ColliderEntry(hitbox, collider);
        activeColliders.computeIfAbsent(group, x -> new ArrayList<>()).add(entry);
        return entry;
    }

    public AutoCloseable registerEntity(Living.Entity collider, Shape hitbox, Affiliation affiliation) {
        var entry = new EntityEntry(hitbox, collider);
        passiveColliders.computeIfAbsent(affiliation, x -> new ArrayList<>()).add(entry);
        return entry;
    }
    
    public Optional<Living.Entity> getRandomEntity(Affiliation affiliation) {
    	var list = passiveColliders.get(affiliation);
    	if(list == null || list.isEmpty())
    		return Optional.empty();
    	return Optional.of(list.get(random.nextInt(list.size()))).map(entry -> entry.entity);
    }

    private abstract class Entry implements AutoCloseable {
        final Shape hitbox;
        boolean dead = false;
        Entry(Shape hitbox) {
            this.hitbox = hitbox;
        }
        @Override
        public final void close() {
            dead = true;
        }
    }

    private class ColliderEntry extends Entry {
        final Collider collider;
        private long decay;
        ColliderEntry(Shape hitbox, Collider collider) {
            super(hitbox);
            this.collider = collider;
        }
        void decay(long x) {
            decay &= ~(1L<<x);
        }
        void tryCollideWith(Battlefield battlefield, EntityEntry entry) {
            final long idMask = 1 << (entry.id & 63);
            if((decay & idMask) == 0 && hitbox.intersects(entry.hitbox)) {
                decay |= idMask;
                collider.collide(battlefield, entry.entity, entry.hitbox);
            }
        }
    }

    private final class EntityEntry extends Entry {
        final Living.Entity entity;
        final long id;
        EntityEntry(Shape hitbox, Living.Entity entity) {
            super(hitbox);
            this.entity = entity;
            this.id = Math.abs((long)random.nextInt());
        }
    }
}
