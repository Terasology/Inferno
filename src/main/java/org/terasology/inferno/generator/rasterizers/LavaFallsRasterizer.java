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
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.WorldRasterizer;
import org.terasology.inferno.generator.facets.InfernoCeilingHeightFacet;
import org.terasology.inferno.generator.facets.InfernoSurfaceHeightFacet;
import org.terasology.inferno.generator.facets.LavaFallsFacet;

public class LavaFallsRasterizer implements WorldRasterizer {
    private static final int LAVA_WELL_DEPTH = 10;

    private Block lava;
    private Random random = new FastRandom();

    @Override
    public void initialize() {
        lava = CoreRegistry.get(BlockManager.class).getBlock("CoreAssets:Lava");
    }

    @Override
    public void generateChunk(Chunk chunk, Region chunkRegion) {
        LavaFallsFacet lavaFallsFacet = chunkRegion.getFacet(LavaFallsFacet.class);
        InfernoSurfaceHeightFacet surfaceFacet = chunkRegion.getFacet(InfernoSurfaceHeightFacet.class);
        InfernoCeilingHeightFacet ceilingFacet = chunkRegion.getFacet(InfernoCeilingHeightFacet.class);

        Vector3i tempPos = new Vector3i();
        for (Vector3ic position : chunk.getRegion()) {
            float surfaceHeight = surfaceFacet.getWorld(position.x(), position.z());
            float ceilingHeight = ceilingFacet.getWorld(position.x(), position.z());

            if (lavaFallsFacet.getWorld(position.x(), position.z())
                    && position.y() > surfaceHeight - LAVA_WELL_DEPTH
                    && position.y() < ceilingHeight + LAVA_WELL_DEPTH) {
                chunk.setBlock(Chunks.toRelative(position, tempPos), lava);
            }
        }
    }
}

