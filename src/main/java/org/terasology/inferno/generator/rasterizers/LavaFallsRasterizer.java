// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inferno.generator.rasterizers;

import org.terasology.engine.math.ChunkMath;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.CoreChunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.WorldRasterizer;
import org.terasology.inferno.generator.facets.InfernoCeilingHeightFacet;
import org.terasology.inferno.generator.facets.InfernoSurfaceHeightFacet;
import org.terasology.inferno.generator.facets.LavaFallsFacet;
import org.terasology.math.geom.Vector3i;

public class LavaFallsRasterizer implements WorldRasterizer {
    private static final int LAVA_WELL_DEPTH = 10;
    private final Random random = new FastRandom();
    private Block lava;

    @Override
    public void initialize() {
        lava = CoreRegistry.get(BlockManager.class).getBlock("CoreAssets:Lava");
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
                chunk.setBlock(ChunkMath.calcRelativeBlockPos(position), lava);
            }
        }
    }
}

