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

import org.terasology.inferno.generator.facets.InfernoCeilingHeightFacet;
import org.terasology.inferno.generator.facets.InfernoSurfaceHeightFacet;
import org.terasology.inferno.generator.facets.LavaFallsFacet;
import org.terasology.math.ChunkMath;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;

public class LavaFallsRasterizer implements WorldRasterizer {
    private static final int LAVA_WELL_DEPTH = 10;

    private Block lava;
    private Random random = new FastRandom();

    @Override
    public void initialize() {
        lava = CoreRegistry.get(BlockManager.class).getBlock("CoreBlocks:Lava");
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        LavaFallsFacet lavaFallsFacet = chunkRegion.getFacet(LavaFallsFacet.class);
        InfernoSurfaceHeightFacet surfaceFacet = chunkRegion.getFacet(InfernoSurfaceHeightFacet.class);
        InfernoCeilingHeightFacet ceilingFacet = chunkRegion.getFacet(InfernoCeilingHeightFacet.class);

        for (Vector3i position : chunk.getRegion()) {
            float surfaceHeight = surfaceFacet.getWorld(position.x, position.z);
            float ceilingHeight = ceilingFacet.getWorld(position.x, position.z);

            if (lavaFallsFacet.getWorld(position.x(), position.z())
                    && position.y > surfaceHeight - LAVA_WELL_DEPTH
                    && position.y < ceilingHeight + LAVA_WELL_DEPTH) {
                chunk.setBlock(ChunkMath.calcBlockPos(position), lava);
            }
        }
    }
}

