// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inferno.generator.rasterizers;

import org.terasology.engine.math.ChunkMath;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.CoreChunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.WorldRasterizer;
import org.terasology.inferno.generator.facets.InfernoCeilingHeightFacet;
import org.terasology.inferno.generator.facets.InfernoSurfaceHeightFacet;
import org.terasology.inferno.generator.facets.LavaLevelFacet;
import org.terasology.math.geom.Vector3i;

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
                chunk.setBlock(ChunkMath.calcRelativeBlockPos(position), dirt);
            } else if (position.y <= ceilingHeight && position.y > surfaceHeight) {
                if (position.y <= lavaLevelFacet.getLavaLevel()) {
                    chunk.setBlock(ChunkMath.calcRelativeBlockPos(position), lava);
                } else {
                    chunk.setBlock(ChunkMath.calcRelativeBlockPos(position), air);
                }
            } else if (position.y <= surfaceHeight) {
                chunk.setBlock(ChunkMath.calcRelativeBlockPos(position), dirt);
            }
        }
    }
}
