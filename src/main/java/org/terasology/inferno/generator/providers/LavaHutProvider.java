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
package org.terasology.inferno.generator.providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.inferno.generator.facets.InfernoCeilingHeightFacet;
import org.terasology.inferno.generator.facets.InfernoSurfaceHeightFacet;
import org.terasology.inferno.generator.facets.LavaHutFacet;
import org.terasology.inferno.generator.facets.LavaLevelFacet;
import org.terasology.inferno.generator.structures.LavaHut;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetBorder;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;

@Produces(LavaHutFacet.class)
@Requires( {
        @Facet(value = InfernoSurfaceHeightFacet.class, border = @FacetBorder(top = 15, bottom = 25, sides = 15)),
        @Facet(InfernoCeilingHeightFacet.class),
        @Facet(LavaLevelFacet.class)
})
public class LavaHutProvider implements FacetProvider {
    private static final int MIN_SPAWN_HEIGHT = 30;
    private static final int MIN_LAVA_PADDING = 10;
    private static final int MIN_HUT_HEIGHT = 6;
    private static final int MAX_HUT_HEIGHT = 12;
    private static final int MIN_HUT_LENGTH = 5;
    private static final int MAX_HUT_LENGTH = 7;
    // odd-only
    private static final int HUT_LENGTH = 5;
    private Noise heightNoise;
    private Noise spawnNoise;
    private Noise hutLengthNoise;
    private Random random;
    private Logger logger = LoggerFactory.getLogger(LavaHutProvider.class);

    @Override
    public void setSeed(long seed) {
        spawnNoise = new WhiteNoise(seed + 50);
        heightNoise = new WhiteNoise(seed - 50);
        hutLengthNoise = new WhiteNoise(seed + 500);
        random = new FastRandom(seed + 50);
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(LavaHutFacet.class).extendBy(20, 30, 20);
        LavaLevelFacet lavaLevelFacet = region.getRegionFacet(LavaLevelFacet.class);
        InfernoSurfaceHeightFacet surfaceHeightFacet = region.getRegionFacet(InfernoSurfaceHeightFacet.class);
        InfernoCeilingHeightFacet ceilingHeightFacet = region.getRegionFacet(InfernoCeilingHeightFacet.class);
        LavaHutFacet lavaHutFacet = new LavaHutFacet(region.getRegion(), border);

        int lavaLevel = lavaLevelFacet.getLavaLevel();
        for (BaseVector2i position : surfaceHeightFacet.getWorldRegion().contents()) {
            float ceilingHeight = ceilingHeightFacet.getWorld(position);
            float hutHeight = lavaLevel + TeraMath.clamp(Math.abs(heightNoise.noise(position.x(), position.y()) * MAX_HUT_HEIGHT), MIN_HUT_HEIGHT, MAX_HUT_HEIGHT);
            if (isOverLava(surfaceHeightFacet, position.x() + MIN_LAVA_PADDING, position.y(), lavaLevel)
                    && isOverLava(surfaceHeightFacet, position.x() - MIN_LAVA_PADDING, position.y(), lavaLevel)
                    && isOverLava(surfaceHeightFacet, position.x(), position.y() + MIN_LAVA_PADDING, lavaLevel)
                    && isOverLava(surfaceHeightFacet, position.x(), position.y() - MIN_LAVA_PADDING, lavaLevel)
                    && ceilingHeight - lavaLevel >= MIN_SPAWN_HEIGHT
                    && spawnNoise.noise(position.x(), position.y()) > 0.999
                    && lavaHutFacet.getWorldRegion().encompasses(position.x(), (int) hutHeight, position.y())) {
                // todo: not hardcoded
                int hutLength;
                if (hutLengthNoise.noise(position.x(), position.y()) < 0.6) {
                    hutLength = 5;
                } else {
                    hutLength = 7;
                }
                lavaHutFacet.setWorld(position.x(), (int) (hutHeight), position.y(), new LavaHut(hutLength));
            }
        }
        region.setRegionFacet(LavaHutFacet.class, lavaHutFacet);
    }

    private boolean isOverLava(InfernoSurfaceHeightFacet surfaceHeightFacet, int x, int y, int lavaLevel) {
        return surfaceHeightFacet.getWorldRegion().contains(x, y)
                && surfaceHeightFacet.getWorld(x, y) < lavaLevel;
    }
}
