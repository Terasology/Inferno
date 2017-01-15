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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.inferno.generator.facets.InfernalTreeFacet;
import org.terasology.inferno.generator.structures.InfernalTree;
import org.terasology.math.ChunkMath;
import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;

import java.util.List;
import java.util.Map;

public class InfernalTreeRasterizer implements WorldRasterizer {
    private Block trunkBlock;
    private Block leafBlock;
    private Logger logger = LoggerFactory.getLogger(InfernalTreeRasterizer.class);


    @Override
    public void initialize() {
        trunkBlock = CoreRegistry.get(BlockManager.class).getBlock("Core:PineTrunk");
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
                Vector3i canopyStart = new Vector3i(pos.x() - blocksFromTrunk, pos.y() - height, pos.z() - blocksFromTrunk);
                Region3i canopyLayerRegion = Region3i.createFromMinAndSize(canopyStart, new Vector3i(blocksFromTrunk * 2 + 1, 1, blocksFromTrunk * 2 + 1));
                for (Vector3i leafPos: canopyLayerRegion) {
                    if (chunk.getRegion().encompasses(leafPos)) {
                        chunk.setBlock(ChunkMath.calcBlockPos(leafPos), leafBlock);
                    }
                }
            }
            for (int height = 0; height < tree.getTrunkHeight(); height++) {
                Vector3i newPos = new Vector3i(pos).subY(height);
                if (chunk.getRegion().encompasses(newPos)) {
                    chunk.setBlock(ChunkMath.calcBlockPos(newPos), trunkBlock);
                }
            }
        }
    }
}
