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
package org.terasology.inferno.generator.rasterizers;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.CoreChunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.WorldRasterizer;
import org.terasology.inferno.generator.facets.FloraFacet;

import java.util.LinkedHashMap;
import java.util.Map;

public class InfernoFloraRasterizer implements WorldRasterizer {
    private Random random = new FastRandom();
    private Map<Block, Double> flora = new LinkedHashMap<>();

    @Override
    public void initialize() {
        flora.put(CoreRegistry.get(BlockManager.class).getBlock("Inferno:FlamingFlower"), 0.45);
        flora.put(CoreRegistry.get(BlockManager.class).getBlock("Inferno:DevilShroom"), 0.45);
        flora.put(CoreRegistry.get(BlockManager.class).getBlock("Inferno:DeadBranch"), 0.1);
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        FloraFacet floraFacet = chunkRegion.getFacet(FloraFacet.class);
        Vector3i tempPos = new Vector3i();
        for (Vector3ic position : chunkRegion.getRegion()) {
            if (floraFacet.getWorld(position)
                    && chunk.getBlock(Chunks.toRelative(position.sub(0, 1, 0, tempPos), tempPos)).getURI() != BlockManager.AIR_ID) {
                chunk.setBlock(Chunks.toRelative(position, tempPos), getRandomFlora());
            }
        }
    }

    // http://stackoverflow.com/a/9330667
    private Block getRandomFlora() {
        double rand = random.nextDouble(0, 1);
        double cumulativeProbability = 0.0;
        for (Map.Entry<Block, Double> entry : flora.entrySet()) {
            cumulativeProbability += entry.getValue();
            if (rand <= cumulativeProbability) {
                return entry.getKey();
            }
        }
        return null;
    }
}
