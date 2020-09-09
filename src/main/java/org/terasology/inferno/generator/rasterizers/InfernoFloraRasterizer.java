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
import org.terasology.inferno.generator.facets.FloraFacet;
import org.terasology.math.geom.Vector3i;

import java.util.LinkedHashMap;
import java.util.Map;

public class InfernoFloraRasterizer implements WorldRasterizer {
    private final Random random = new FastRandom();
    private final Map<Block, Double> flora = new LinkedHashMap<>();

    @Override
    public void initialize() {
        flora.put(CoreRegistry.get(BlockManager.class).getBlock("Inferno:FlamingFlower"), 0.45);
        flora.put(CoreRegistry.get(BlockManager.class).getBlock("Inferno:DevilShroom"), 0.45);
        flora.put(CoreRegistry.get(BlockManager.class).getBlock("Inferno:DeadBranch"), 0.1);
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        FloraFacet floraFacet = chunkRegion.getFacet(FloraFacet.class);
        for (Vector3i position : chunkRegion.getRegion()) {
            if (floraFacet.getWorld(position)
                    && chunk.getBlock(ChunkMath.calcRelativeBlockPos(new Vector3i(position).subY(1))).getURI() != BlockManager.AIR_ID) {
                chunk.setBlock(ChunkMath.calcRelativeBlockPos(position), getRandomFlora());
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
