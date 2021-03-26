// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inferno.generator.rasterizers;

import org.joml.Vector3ic;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.WorldRasterizer;
import org.terasology.inferno.generator.facets.CaveFacet;

/**
 * Still need this rasterizer because just changing the density does not provide the correct effect with the default perlin generator
 */
public class CaveRasterizer implements WorldRasterizer {
    String blockUri;

    public CaveRasterizer() {
    }

    public CaveRasterizer(String blockUri) {
        this.blockUri = blockUri;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void generateChunk(Chunk chunk, Region chunkRegion) {
        CaveFacet caveFacet = chunkRegion.getFacet(CaveFacet.class);

        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        Block caveBlock = blockManager.getBlock(BlockManager.AIR_ID);
        if (blockUri != null) {
            caveBlock = blockManager.getBlock(blockUri);
        }

        for (Vector3ic position : Chunks.CHUNK_REGION) {
            if (caveFacet.get(position)) {
                chunk.setBlock(position, caveBlock);
            }
        }
    }
}
