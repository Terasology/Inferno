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
import org.terasology.inferno.generator.facets.LavaHutFacet;
import org.terasology.inferno.generator.facets.LavaLevelFacet;
import org.terasology.inferno.generator.structures.LavaHut;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.chunks.Chunks;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;

import java.util.Map;

public class LavaHutRasterizer implements WorldRasterizer {
    // probabilities of blocks being placed/spawned - random placing mimics dilapidation
    private static final float WALL_BLOCK_PROB = 0.85f;
    private static final float WALL_TOP_BLOCK_PROB = 0.65f;
    private static final float UPPER_CRACKED_BLOCK_PROB = 0.35f;
    private static final float LOWER_CRACKED_BLOCK_PROB = 0.1f;
    private static final float PLATFORM_BLOCK_PROB = 0.9f;
    private static final float LAVA_SPAWN_PROB = 0.15f;

    private Block topBlock;
    private Block topBlockCracked;
    private Block lowerBlock;
    private Block lowerBlockCracked;
    private Block lava;
    private Random random = new FastRandom();

    @Override
    public void initialize() {
        topBlock = CoreRegistry.get(BlockManager.class).getBlock("ChiselBlocks:Basalt_bricks-small");
        topBlockCracked = CoreRegistry.get(BlockManager.class).getBlock("ChiselBlocks:Basalt_bricks-triple");
        lowerBlock = CoreRegistry.get(BlockManager.class).getBlock("ChiselBlocks:Basalt_tiles-large");
        lowerBlockCracked = CoreRegistry.get(BlockManager.class).getBlock("ChiselBlocks:Basalt_cracked");
        lava = CoreRegistry.get(BlockManager.class).getBlock("CoreAssets:Lava");
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        LavaHutFacet lavaHutFacet = chunkRegion.getFacet(LavaHutFacet.class);
        LavaLevelFacet lavaLevelFacet = chunkRegion.getFacet(LavaLevelFacet.class);

        for (Map.Entry<Vector3ic, LavaHut> entry : lavaHutFacet.getWorldEntries().entrySet()) {
            Vector3i position = new Vector3i(entry.getKey());
            LavaHut lavaHut = entry.getValue();
            if (lavaHut != null && chunk.getRegion().contains(position)) {
                Vector3ic dirVector = lavaHut.getHutDirection().asVector3i();
                int length = lavaHut.getLength();
                int height = lavaHut.getHeight();
                // since length is odd, fromCenter gets rounded down
                int toOuterCenter = length / 2 + 1;
                int toInnerCenter = length / 2;

                // platform
                BlockRegion platformRegion = new BlockRegion(position).expand(toOuterCenter, 0, toOuterCenter);
                BlockRegion backPlatformRegion = new BlockRegion(position.add(new Vector3i(dirVector).mul(toOuterCenter))).expand(0, 0, toOuterCenter);
                if (!isEncompassed(platformRegion, chunkRegion.getRegion())) {
                    continue;
                }
                Vector3i tempPos = new Vector3i();
                for (Vector3ic blockPos : platformRegion) {
                    float platformBlockProb = random.nextFloat();
                    if (chunkRegion.getRegion().contains(blockPos) && !backPlatformRegion.contains(blockPos) && platformBlockProb <= PLATFORM_BLOCK_PROB) {
                        chunk.setBlock(Chunks.toRelative(blockPos, tempPos), topBlock);
                    }
                }

                // walls except top layer
                Vector3i outerMin = new Vector3i(position).sub(toInnerCenter, 0, toInnerCenter);
                BlockRegion outerWallRegion = new BlockRegion(outerMin).setSize(length, height, length);
                Vector3i innerMin = new Vector3i(outerMin).add(1, 0, 1);
                BlockRegion innerWallRegion = new BlockRegion(innerMin).setSize(length - 2, height + 2, length - 2);
                for (Vector3ic blockPosition : outerWallRegion) {
                    Vector3i blockPos = new Vector3i(blockPosition);
                    if (chunkRegion.getRegion().contains(blockPos) && !innerWallRegion.contains(blockPos) && random.nextFloat() <= WALL_BLOCK_PROB) {
                        placeWithProbability(chunk, blockPos, topBlockCracked, topBlock, UPPER_CRACKED_BLOCK_PROB);
                    }
                }
                // lava columns
                BlockRegion topOuterWallRegion = new BlockRegion(innerMin.add(0, height - 1, 0)).setSize(length - 2, 1, length - 2);
                BlockRegion topInnerWallRegion = new BlockRegion(innerMin.add(1, height - 1, 1)).setSize(length - 3, 1, length - 3);
                for (Vector3ic blockPos : topOuterWallRegion) {
                    if (chunkRegion.getRegion().contains(blockPos) && !topInnerWallRegion.contains(blockPos) && random.nextFloat() <= LAVA_SPAWN_PROB) {
                        Vector3i lavaPos = new Vector3i(blockPos);
                        while (chunk.getRegion().contains(lavaPos) && lavaPos.y() >= lavaLevelFacet.getLavaLevel()) {
                            chunk.setBlock(Chunks.toRelative(lavaPos, new Vector3i()), lava);
                            lavaPos.sub(0,1,0);
                        }
                    }
                }

                // top layer
                BlockRegion topLayerWallRegion = new BlockRegion(position.add(0,height,0)).expand(toInnerCenter, 0, toInnerCenter);
                for (Vector3ic blockPosition : topLayerWallRegion) {
                    Vector3i blockPos = new Vector3i(blockPosition);
                    if (chunkRegion.getRegion().contains(blockPos) && !innerWallRegion.contains(blockPos) && random.nextFloat() <= WALL_TOP_BLOCK_PROB) {
                        placeWithProbability(chunk, blockPos, topBlockCracked, topBlock, UPPER_CRACKED_BLOCK_PROB);
                    }
                }

                // stilts
                Vector3i stiltsCenter = new Vector3i(position).add(0,height - 1,0);
                Block stiltBlock;
                while (chunkRegion.getRegion().contains(stiltsCenter) && stiltsCenter.y() >= lavaLevelFacet.getLavaLevel()) {
                    if (stiltsCenter.y() >= position.y()) {
                        chunk.setBlock(Chunks.toRelative(stiltsCenter.add(toInnerCenter, 0, toInnerCenter, tempPos), tempPos), topBlock);
                        chunk.setBlock(Chunks.toRelative(stiltsCenter.add(-toInnerCenter, 0, toInnerCenter, tempPos), tempPos), topBlock);
                        chunk.setBlock(Chunks.toRelative(stiltsCenter.add(toInnerCenter, 0, -toInnerCenter, tempPos), tempPos), topBlock);
                        chunk.setBlock(Chunks.toRelative(stiltsCenter.add(-toInnerCenter, 0, -toInnerCenter, tempPos), tempPos), topBlock);
                    } else {
                        placeWithProbability(chunk, stiltsCenter.add(toInnerCenter, 0, toInnerCenter, tempPos), lowerBlockCracked, lowerBlock, LOWER_CRACKED_BLOCK_PROB);
                        placeWithProbability(chunk, stiltsCenter.add(-toInnerCenter, 0, toInnerCenter, tempPos), lowerBlockCracked, lowerBlock, LOWER_CRACKED_BLOCK_PROB);
                        placeWithProbability(chunk, stiltsCenter.add(toInnerCenter, 0, -toInnerCenter, tempPos), lowerBlockCracked, lowerBlock, LOWER_CRACKED_BLOCK_PROB);
                        placeWithProbability(chunk, stiltsCenter.add(-toInnerCenter, 0, -toInnerCenter, tempPos), lowerBlockCracked, lowerBlock, LOWER_CRACKED_BLOCK_PROB);
                    }
                    stiltsCenter.sub(0,1,0);
                }
            }
        }
    }

    private boolean isEncompassed(BlockRegion checkRegion, BlockRegion chunkRegion) {
        for (Vector3ic pos : checkRegion) {
            if (!chunkRegion.contains(pos)) {
                return false;
            }
        }
        return true;
    }

    private void placeWithProbability(CoreChunk chunk, Vector3i pos, Block block1, Block block2, float prob) {
        if (random.nextFloat() <= prob) {
            chunk.setBlock(Chunks.toRelative(pos, new Vector3i()), block1);
        } else {
            chunk.setBlock(Chunks.toRelative(pos, new Vector3i()), block2);
        }
    }
}
