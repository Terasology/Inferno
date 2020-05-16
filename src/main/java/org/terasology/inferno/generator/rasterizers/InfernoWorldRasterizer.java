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
import org.terasology.inferno.generator.facets.LavaLevelFacet;
import org.terasology.math.ChunkMath;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;

import static org.terasology.inferno.generator.InfernoWorldGenerator.INFERNO_BORDER;

public class InfernoWorldRasterizer implements WorldRasterizer {
    private Block dirt;
    private Block air;
    private Block lava;

    @Override
    public void initialize() {
        dirt = CoreRegistry.get(BlockManager.class).getBlock("Inferno:BloodiedStone");
        air = CoreRegistry.get(BlockManager.class).getBlock(BlockManager.AIR_ID);
        lava = CoreRegistry.get(BlockManager.class).getBlock("CoreAssets:Lava");
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        InfernoSurfaceHeightFacet surfaceFacet = chunkRegion.getFacet(InfernoSurfaceHeightFacet.class);
        InfernoCeilingHeightFacet ceilingFacet = chunkRegion.getFacet(InfernoCeilingHeightFacet.class);
        LavaLevelFacet lavaLevelFacet = chunkRegion.getFacet(LavaLevelFacet.class);

        for (Vector3i position : chunkRegion.getRegion()) {
            float surfaceHeight = surfaceFacet.getWorld(position.x, position.z);
            float ceilingHeight = ceilingFacet.getWorld(position.x, position.z);

            if (position.y > ceilingHeight && position.y < ceilingHeight + INFERNO_BORDER) {
                chunk.setBlock(ChunkMath.calcBlockPos(position), dirt);
            } else if (position.y <= ceilingHeight && position.y > surfaceHeight) {
                if (position.y <= lavaLevelFacet.getLavaLevel()) {
                    chunk.setBlock(ChunkMath.calcBlockPos(position), lava);
                } else {
                    chunk.setBlock(ChunkMath.calcBlockPos(position), air);
                }
            } else if (position.y <= surfaceHeight) {
                chunk.setBlock(ChunkMath.calcBlockPos(position), dirt);
            }
        }
    }
}
