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

import com.google.common.collect.Lists;
import org.joml.Vector2ic;
import org.terasology.engine.math.Direction;
import org.terasology.engine.utilities.procedural.Noise;
import org.terasology.engine.utilities.procedural.WhiteNoise;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetBorder;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.inferno.generator.facets.InfernoCeilingHeightFacet;
import org.terasology.inferno.generator.facets.InfernoSurfaceHeightFacet;
import org.terasology.inferno.generator.facets.LavaHutFacet;
import org.terasology.inferno.generator.facets.LavaLevelFacet;
import org.terasology.inferno.generator.structures.LavaHut;
import org.terasology.math.TeraMath;

import java.util.List;

@Produces(LavaHutFacet.class)
@Requires( {
        @Facet(value = InfernoSurfaceHeightFacet.class, border = @FacetBorder(top = 15, bottom = 25, sides = 15)),
        @Facet(InfernoCeilingHeightFacet.class),
        @Facet(LavaLevelFacet.class)
})
public class LavaHutProvider implements FacetProvider {
    private static final List<Direction> HORIZONTAL_DIRECTIONS = Lists.newArrayList(Direction.FORWARD, Direction.BACKWARD, Direction.LEFT, Direction.RIGHT);
    private static final int MIN_SPAWN_HEIGHT = 25;
    private static final int MIN_LAVA_PADDING = 15;
    private static final int MIN_HUT_HEIGHT = 5;
    private static final int MAX_HUT_HEIGHT = 8;

    private Noise heightNoise;
    private Noise spawnNoise;
    private Noise hutLengthNoise;
    private Noise dirNoise;

    @Override
    public void setSeed(long seed) {
        spawnNoise = new WhiteNoise(seed + 51);
        heightNoise = new WhiteNoise(seed + 52);
        hutLengthNoise = new WhiteNoise(seed + 53);
        dirNoise = new WhiteNoise(seed + 54);
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(LavaHutFacet.class).extendBy(20, 30, 20);
        LavaLevelFacet lavaLevelFacet = region.getRegionFacet(LavaLevelFacet.class);
        InfernoSurfaceHeightFacet surfaceHeightFacet = region.getRegionFacet(InfernoSurfaceHeightFacet.class);
        InfernoCeilingHeightFacet ceilingHeightFacet = region.getRegionFacet(InfernoCeilingHeightFacet.class);
        LavaHutFacet lavaHutFacet = new LavaHutFacet(region.getRegion(), border);

        int lavaLevel = lavaLevelFacet.getLavaLevel();
        for (Vector2ic position : surfaceHeightFacet.getWorldArea()) {
            float ceilingHeight = ceilingHeightFacet.getWorld(position);
            float hutHeight = lavaLevel + TeraMath.clamp(Math.abs(heightNoise.noise(position.x(), position.y()) * MAX_HUT_HEIGHT), MIN_HUT_HEIGHT, MAX_HUT_HEIGHT);
            if (isOverLava(surfaceHeightFacet, position.x() + MIN_LAVA_PADDING, position.y(), lavaLevel)
                    && isOverLava(surfaceHeightFacet, position.x() - MIN_LAVA_PADDING, position.y(), lavaLevel)
                    && isOverLava(surfaceHeightFacet, position.x(), position.y() + MIN_LAVA_PADDING, lavaLevel)
                    && isOverLava(surfaceHeightFacet, position.x(), position.y() - MIN_LAVA_PADDING, lavaLevel)
                    && ceilingHeight - lavaLevel >= MIN_SPAWN_HEIGHT
                    && spawnNoise.noise(position.x(), position.y()) > 0.998
                    && lavaHutFacet.getWorldRegion().contains(position.x(), (int) hutHeight, position.y())) {
                // todo: not hardcoded
                int hutLength;
                float lengthNoiseVal = Math.abs(hutLengthNoise.noise(position.x(), position.y()));
                LavaHut lavaHut = new LavaHut();
                if (lengthNoiseVal <= 0.3) {
                    hutLength = 5;
                } else if (lengthNoiseVal <= 0.8) {
                    hutLength = 7;
                } else {
                    hutLength = 9;
                }
                lavaHut.setLength(hutLength);
                int dirIndex = Math.abs(Math.round(dirNoise.noise(position.x(), position.y()) * (HORIZONTAL_DIRECTIONS.size() - 1)));
                lavaHut.setHutDirection(HORIZONTAL_DIRECTIONS.get(dirIndex));
                lavaHutFacet.setWorld(position.x(), (int) (hutHeight), position.y(), lavaHut);
            }
        }
        region.setRegionFacet(LavaHutFacet.class, lavaHutFacet);
    }

    private boolean isOverLava(InfernoSurfaceHeightFacet surfaceHeightFacet, int x, int y, int lavaLevel) {
        return surfaceHeightFacet.getWorldArea().contains(x, y)
                && surfaceHeightFacet.getWorld(x, y) < lavaLevel;
    }
}
