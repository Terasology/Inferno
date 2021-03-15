/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.inferno.world;

import org.joml.RoundingMode;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3i;
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
import org.terasology.engine.logic.health.BeforeDestroyEvent;
import org.terasology.logic.health.event.RestoreFullHealthEvent;
import org.terasology.module.inventory.systems.InventoryManager;
import org.terasology.module.inventory.systems.InventoryUtils;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.World;
import org.terasology.engine.world.generator.WorldGenerator;
import org.terasology.inferno.generator.facets.InfernoCeilingHeightFacet;
import org.terasology.inferno.generator.facets.InfernoSurfaceHeightFacet;
import org.terasology.inferno.generator.facets.LavaLevelFacet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.terasology.inferno.generator.InfernoZonePlugin.INFERNO_DEPTH;

@RegisterSystem(RegisterMode.CLIENT)
public class InfernoClientSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

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

    private Map<EntityRef, Vector3f> teleportQueue = new HashMap<>();

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
            Vector3f spawnPos = findInfernoSpawn(locationComponent.getWorldPosition(new Vector3f()));
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
        BlockRegion searchArea = new BlockRegion(new org.joml.Vector3i(new org.joml.Vector3f(currentPos.x(),
                -INFERNO_DEPTH, currentPos.z()), RoundingMode.FLOOR)).expand(searchRadius);
        Region worldRegion = world.getWorldData(searchArea);

        InfernoSurfaceHeightFacet surfaceHeightFacet = worldRegion.getFacet(InfernoSurfaceHeightFacet.class);
        InfernoCeilingHeightFacet ceilingHeightFacet = worldRegion.getFacet(InfernoCeilingHeightFacet.class);
        LavaLevelFacet lavaLevelFacet = worldRegion.getFacet(LavaLevelFacet.class);
        if (surfaceHeightFacet != null && ceilingHeightFacet != null) {
            for (Vector2ic pos : surfaceHeightFacet.getWorldArea()) {
                float surfaceHeight = surfaceHeightFacet.getWorld(pos);
                if (surfaceHeight < ceilingHeightFacet.getWorld(pos) - 1 && surfaceHeight > lavaLevelFacet.getLavaLevel()) {
                    return new Vector3f(pos.x(), surfaceHeight + 1, pos.y());
                }
            }
        }
        return null;
    }
}
