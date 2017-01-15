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

import org.terasology.inferno.generator.facets.InfernoSurfaceHeightFacet;
import org.terasology.inferno.generator.facets.LavaHutFacet;
import org.terasology.inferno.generator.facets.LavaLevelFacet;
import org.terasology.inferno.generator.structures.LavaHut;
import org.terasology.math.ChunkMath;
import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;

import java.util.Map;

public class LavaHutRasterizer implements WorldRasterizer {
    // probabilities of blocks being placed/spawned - random placing mimics dilapidation
    private static final float WALL_BLOCK_PROB = 0.9f;
    private static final float WALL_TOP_BLOCK_PROB = 0.4f;
    private static final float CRACKED_BLOCK_PROB = 0.5f;
    private static final float PLATFORM_BLOCK_PROB = 0.95f;
    private Block topBlock;
    private Block topBlockCracked;
    private Block lowerBlock;
    private Block air;
    private Random random = new FastRandom();

    @Override
    public void initialize() {
        topBlock = CoreRegistry.get(BlockManager.class).getBlock("ChiselBlocks:Basalt_bricks-soft");
        topBlockCracked = CoreRegistry.get(BlockManager.class).getBlock("ChiselBlocks:Basalt_bricks-cracked");
        lowerBlock = CoreRegistry.get(BlockManager.class).getBlock("ChiselBlocks:Basalt_pillar-side");
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        LavaHutFacet lavaHutFacet = chunkRegion.getFacet(LavaHutFacet.class);
        LavaLevelFacet lavaLevelFacet = chunkRegion.getFacet(LavaLevelFacet.class);

        for (Map.Entry<BaseVector3i, LavaHut> entry : lavaHutFacet.getWorldEntries().entrySet()) {
            Vector3i position = new Vector3i(entry.getKey());
            LavaHut lavaHut = entry.getValue();
            if (lavaHut != null && chunk.getRegion().encompasses(position)) {
                int length = lavaHut.getLength();
                int height = lavaHut.getHeight();
                // since length is odd, fromCenter gets rounded down
                int toOuterCenter = length / 2 + 1;
                int toInnerCenter = length / 2;

                // platform
                Region3i platformRegion = Region3i.createFromCenterExtents(position, new Vector3i(toOuterCenter, 0, toOuterCenter));
                Region3i backPlatformRegion = Region3i.createFromCenterExtents(new Vector3i(position).addX(toOuterCenter), new Vector3i(0, 0, toOuterCenter));
                if (!isEncompassed(platformRegion, chunkRegion.getRegion())) {
                    continue;
                }
                for (Vector3i blockPos : platformRegion) {
                    if (chunkRegion.getRegion().encompasses(blockPos) && !backPlatformRegion.encompasses(blockPos) && random.nextFloat() < PLATFORM_BLOCK_PROB) {
                        chunk.setBlock(ChunkMath.calcBlockPos(blockPos), topBlock);
                    }
                }

                // walls except top layer
                Vector3i outerMin = new Vector3i(position).sub(toInnerCenter, 0, toInnerCenter);
                Region3i outerWallRegion = Region3i.createFromMinAndSize(outerMin, new Vector3i(length, height, length));
                Vector3i innerMin = new Vector3i(outerMin).add(1, 0, 1);
                Region3i innerWallRegion = Region3i.createFromMinAndSize(innerMin, new Vector3i(length - 2, height + 2, length - 2));
                for (Vector3i blockPos : outerWallRegion) {
                    if (chunkRegion.getRegion().encompasses(blockPos) && !innerWallRegion.encompasses(blockPos) && random.nextFloat() < WALL_BLOCK_PROB) {
                        if (random.nextFloat() < CRACKED_BLOCK_PROB) {
                            chunk.setBlock(ChunkMath.calcBlockPos(blockPos), topBlockCracked);
                        }
                        else {
                            chunk.setBlock(ChunkMath.calcBlockPos(blockPos), topBlock);
                        }
                    }
                }

                // top layer
                Region3i topLayerWallRegion = Region3i.createFromCenterExtents(new Vector3i(position).addY(height), new Vector3i(toInnerCenter, 0, toInnerCenter));
                for (Vector3i blockPos : topLayerWallRegion) {
                    if (chunkRegion.getRegion().encompasses(blockPos) && !innerWallRegion.encompasses(blockPos) && random.nextFloat() < WALL_TOP_BLOCK_PROB) {
                        chunk.setBlock(ChunkMath.calcBlockPos(blockPos), topBlock);
                    }
                }

                // stilts
                Vector3i stiltsCenter = new Vector3i(position).addY(height - 1);
                Block stiltBlock;
                while (chunkRegion.getRegion().encompasses(stiltsCenter) && stiltsCenter.y() > lavaLevelFacet.getLavaLevel()) {
                    if (stiltsCenter.y() >= position.y()) {
                        stiltBlock = topBlock;
                    }
                    else {
                        stiltBlock = lowerBlock;
                    }
                    chunk.setBlock(ChunkMath.calcBlockPos(new Vector3i(stiltsCenter).add(toInnerCenter, 0, toInnerCenter)), stiltBlock);
                    chunk.setBlock(ChunkMath.calcBlockPos(new Vector3i(stiltsCenter).add(-toInnerCenter, 0, toInnerCenter)), stiltBlock);
                    chunk.setBlock(ChunkMath.calcBlockPos(new Vector3i(stiltsCenter).add(toInnerCenter, 0, -toInnerCenter)), stiltBlock);
                    chunk.setBlock(ChunkMath.calcBlockPos(new Vector3i(stiltsCenter).add(-toInnerCenter, 0, -toInnerCenter)), stiltBlock);
                    stiltsCenter.subY(1);
                }
            }
        }
    }

    private boolean isEncompassed(Region3i checkRegion, Region3i chunkRegion) {
        for (Vector3i pos : checkRegion) {
            if (!chunkRegion.encompasses(pos)) {
                return false;
            }
        }
        return true;
    }
}
