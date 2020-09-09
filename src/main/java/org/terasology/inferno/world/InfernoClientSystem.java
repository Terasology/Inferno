// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inferno.world;

import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.audio.events.PlaySoundEvent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.characters.CharacterTeleportEvent;
import org.terasology.engine.logic.destruction.BeforeDestroyEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.World;
import org.terasology.engine.world.generator.WorldGenerator;
import org.terasology.health.logic.event.RestoreFullHealthEvent;
import org.terasology.inferno.generator.facets.InfernoCeilingHeightFacet;
import org.terasology.inferno.generator.facets.InfernoSurfaceHeightFacet;
import org.terasology.inferno.generator.facets.LavaLevelFacet;
import org.terasology.inventory.logic.InventoryManager;
import org.terasology.inventory.logic.InventoryUtils;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.terasology.inferno.generator.InfernoWorldGenerator.INFERNO_DEPTH;

@RegisterSystem(RegisterMode.CLIENT)
public class InfernoClientSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private final Map<EntityRef, Vector3f> teleportQueue = new HashMap<>();
    @In
    private WorldProvider worldProvider;
    @In
    private LocalPlayer localPlayer;
    @In
    private WorldGenerator worldGenerator;
    @In
    private InventoryManager inventoryManager;
    @In
    private AudioManager audioManager;

    @Override
    public void update(float delta) {
        if (!teleportQueue.isEmpty()) {
            Iterator<Map.Entry<EntityRef, Vector3f>> teleportIt = teleportQueue.entrySet().iterator();
            while (teleportIt.hasNext()) {
                Map.Entry<EntityRef, Vector3f> entry = teleportIt.next();
                EntityRef character = entry.getKey();
                Vector3f targetPos = entry.getValue();
                character.send(new CharacterTeleportEvent(targetPos));
                teleportIt.remove();
            }
        }
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH)
    public void onDeath(BeforeDestroyEvent event, EntityRef entity, CharacterComponent characterComponent,
                        LocationComponent locationComponent) {
        EntityRef character = localPlayer.getCharacterEntity();
        EntityRef client = localPlayer.getClientEntity();
        EntityRef item = EntityRef.NULL;
        boolean resurrect = false;
        for (int i = 0; i < InventoryUtils.getSlotCount(character); i++) {
            if (InventoryUtils.getItemAt(character, i).hasComponent(InfernoResurrectComponent.class)) {
                resurrect = true;
                item = InventoryUtils.getItemAt(character, i);
                break;
            }
        }
        if (resurrect) {
            event.consume();
            Vector3f spawnPos = findInfernoSpawn(locationComponent.getWorldPosition());
            if (spawnPos != null) {
                inventoryManager.removeItem(entity, entity, item, true);
                character.send(new RestoreFullHealthEvent(character));
                character.send(new PlaySoundEvent(Assets.getSound("Inferno:EnterPortal").get(), 0.4f));
                character.send(new EnterInfernoEvent(client));
                teleportQueue.put(character, spawnPos);
            }
        }
    }

    @ReceiveEvent
    public void onEnterInfeno(EnterInfernoEvent event, EntityRef entity) {
        // temp-fix, not ideal
        entity.send(new PlaySoundEvent(Assets.getSound("Inferno:InfernoAmbience").get(), 0.7f));
        //audioManager.playMusic(Assets.getMusic("Inferno:InfernoAmbience").get(), 1f);
    }

    private Vector3f findInfernoSpawn(Vector3f currentPos) {
        World world = worldGenerator.getWorld();
        Vector3i searchRadius = new Vector3i(32, 1, 32);
        Region3i searchArea = Region3i.createFromCenterExtents(new Vector3i(currentPos.x(), -INFERNO_DEPTH,
                currentPos.z()), searchRadius);
        Region worldRegion = world.getWorldData(searchArea);

        InfernoSurfaceHeightFacet surfaceHeightFacet = worldRegion.getFacet(InfernoSurfaceHeightFacet.class);
        InfernoCeilingHeightFacet ceilingHeightFacet = worldRegion.getFacet(InfernoCeilingHeightFacet.class);
        LavaLevelFacet lavaLevelFacet = worldRegion.getFacet(LavaLevelFacet.class);
        if (surfaceHeightFacet != null && ceilingHeightFacet != null) {
            for (BaseVector2i pos : surfaceHeightFacet.getWorldRegion().contents()) {
                float surfaceHeight = surfaceHeightFacet.getWorld(pos);
                if (surfaceHeight < ceilingHeightFacet.getWorld(pos) - 1 && surfaceHeight > lavaLevelFacet.getLavaLevel()) {
                    return new Vector3f(pos.x(), surfaceHeight + 1, pos.y());
                }
            }
        }
        return null;
    }
}
