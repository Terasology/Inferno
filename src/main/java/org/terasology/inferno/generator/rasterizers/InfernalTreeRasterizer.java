// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inferno.generator.rasterizers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.math.ChunkMath;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.CoreChunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.WorldRasterizer;
import org.terasology.inferno.generator.facets.InfernalTreeFacet;
import org.terasology.inferno.generator.structures.InfernalTree;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector3i;

import java.util.List;
import java.util.Map;

public class InfernalTreeRasterizer implements WorldRasterizer {
    private final Logger logger = LoggerFactory.getLogger(InfernalTreeRasterizer.class);
    private Block trunkBlock;
    private Block leafBlock;

    @Override
    public void initialize() {
        trunkBlock = CoreRegistry.get(BlockManager.class).getBlock("CoreAssets:PineTrunk");
        leafBlock = CoreRegistry.get(BlockManager.class).getBlock("Inferno:FireLeaf");
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        InfernalTreeFacet treeFacet = chunkRegion.getFacet(InfernalTreeFacet.class);

        for (Map.Entry<BaseVector3i, InfernalTree> entry : treeFacet.getWorldEntries().entrySet()) {
            Vector3i pos = new Vector3i(entry.getKey());
            InfernalTree tree = entry.getValue();
            List<Integer> canopyLayers = tree.getCanopyLayers();
//            // no floating trees
//            if (!chunkRegion.getRegion().encompasses(pos)) {
//                continue;
//            }

            for (int height = 0; height < canopyLayers.size(); height++) {
                int blocksFromTrunk = canopyLayers.get(height);
                if (blocksFromTrunk < 0) {
                    continue;
                }
                Vector3i canopyStart = new Vector3i(pos.x() - blocksFromTrunk, pos.y() - height,
                        pos.z() - blocksFromTrunk);
                Region3i canopyLayerRegion = Region3i.createFromMinAndSize(canopyStart,
                        new Vector3i(blocksFromTrunk * 2 + 1, 1, blocksFromTrunk * 2 + 1));
                for (Vector3i leafPos : canopyLayerRegion) {
                    if (chunk.getRegion().encompasses(leafPos)) {
                        chunk.setBlock(ChunkMath.calcRelativeBlockPos(leafPos), leafBlock);
                    }
                }
            }
            for (int height = 0; height < tree.getTrunkHeight(); height++) {
                Vector3i newPos = new Vector3i(pos).subY(height);
                if (chunk.getRegion().encompasses(newPos)) {
                    chunk.setBlock(ChunkMath.calcRelativeBlockPos(newPos), trunkBlock);
                }
            }
        }
    }
}
