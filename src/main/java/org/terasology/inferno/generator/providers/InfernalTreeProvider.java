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

import org.joml.Vector2f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.inferno.generator.facets.InfernalTreeFacet;
import org.terasology.inferno.generator.facets.InfernoCeilingHeightFacet;
import org.terasology.inferno.generator.facets.InfernoSurfaceHeightFacet;
import org.terasology.inferno.generator.structures.InfernalTree;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.utilities.procedural.BrownianNoise;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.SimplexNoise;
import org.terasology.utilities.procedural.SubSampledNoise;
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

import java.util.ArrayList;
import java.util.List;

@Produces(InfernalTreeFacet.class)
@Requires( {
        @Facet(value = InfernoCeilingHeightFacet.class, border = @FacetBorder(top = 25, bottom = 25, sides = 25)),
        @Facet(InfernoSurfaceHeightFacet.class)
})
public class InfernalTreeProvider implements FacetProvider {
    private static final int MIN_TRUNK_HEIGHT = 4;
    private static final int MAX_TRUNK_HEIGHT = 6;
    private static final int MIN_SPAWN_SPACE = 15;

    private Noise spawnNoise;
    private Noise heightNoise;
    private Random random;
    private Logger logger = LoggerFactory.getLogger(InfernalTreeProvider.class);

    @Override
    public void setSeed(long seed) {
        spawnNoise = new WhiteNoise(seed + 20);
        heightNoise = new SubSampledNoise(new BrownianNoise(new SimplexNoise(seed + 30), 8), new Vector2f(0.001f, 0.001f), 1);
        random = new FastRandom(seed + 20);
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(InfernalTreeFacet.class).extendBy(30, 30, 30);
        InfernalTreeFacet treeFacet = new InfernalTreeFacet(region.getRegion(), border);
        InfernoCeilingHeightFacet ceilingHeightFacet = region.getRegionFacet(InfernoCeilingHeightFacet.class);
        InfernoSurfaceHeightFacet surfaceHeightFacet = region.getRegionFacet(InfernoSurfaceHeightFacet.class);

        for (BaseVector2i position : ceilingHeightFacet.getWorldRegion().contents()) {
            int ceilingHeight = (int) Math.floor(ceilingHeightFacet.getWorld(position));
            float surfaceHeight = surfaceHeightFacet.getWorld(position);
            if (treeFacet.getWorldRegion().contains(position.x(), ceilingHeight, position.y())
                    && ceilingHeight - surfaceHeight > MIN_SPAWN_SPACE
                    && spawnNoise.noise(position.x(), position.y()) > 0.997) {
                int trunkHeight = (int) TeraMath.clamp(heightNoise.noise(position.x(), position.y()) * MAX_TRUNK_HEIGHT, MIN_TRUNK_HEIGHT, MAX_TRUNK_HEIGHT);
                Vector3ic treePos = new Vector3i(position.x(), ceilingHeight, position.y());
                InfernalTree tree = new InfernalTree(trunkHeight, generateCanopy(trunkHeight));
                treeFacet.setWorld(treePos, tree);
                treeFacet.addTree(treePos);
            }
        }
        region.setRegionFacet(InfernalTreeFacet.class, treeFacet);
    }

    private List<Integer> generateCanopy(int trunkHeight) {
        List<Integer> canopySize = new ArrayList<>();
        int canopyStart = trunkHeight / 2 + 1;
        int canopyCount = trunkHeight - canopyStart + 1;
        for (int height = 0; height < trunkHeight + 1; height++) {
            if (height >= canopyStart) {
                canopySize.add(canopyCount);
                canopyCount--;
            } else {
                canopySize.add(0);
            }
        }
        return canopySize;
    }
}
