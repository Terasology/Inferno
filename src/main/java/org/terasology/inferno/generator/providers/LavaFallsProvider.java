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

import org.joml.Vector2ic;
import org.terasology.inferno.generator.facets.LavaFallsFacet;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.block.BlockAreac;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;

@Produces(LavaFallsFacet.class)
public class LavaFallsProvider implements FacetProvider {

    private Noise spawnNoise;
    private Noise sizeNoise1;
    private Noise sizeNoise2;
    private Noise sizeNoise3;
    private Noise sizeNoise4;
    private Random random;

    @Override
    public void setSeed(long seed) {
        spawnNoise = new WhiteNoise(seed + 4);
        sizeNoise1 = new WhiteNoise(seed - 40);
        sizeNoise2 = new WhiteNoise(seed - 41);
        sizeNoise3 = new WhiteNoise(seed - 42);
        sizeNoise4 = new WhiteNoise(seed - 43);
        random = new FastRandom(seed + 4);
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(LavaFallsFacet.class).extendBy(4, 4, 4);
        LavaFallsFacet facet = new LavaFallsFacet(region.getRegion(), border);

        BlockAreac processRegion = facet.getWorldArea();
        for (Vector2ic position : processRegion) {
            if (spawnNoise.noise(position.x(), position.y()) > 0.9995) {
                float noise1 = sizeNoise1.noise(position.x(), position.y());
                float noise2 = sizeNoise2.noise(position.x(), position.y());
                float noise3 = sizeNoise3.noise(position.x(), position.y());
                float noise4 = sizeNoise4.noise(position.x(), position.y());

                if (noise1 > 0 && facet.getWorldArea().contains(position.x(), position.y())) {
                    facet.setWorld(position.x(), position.y(), true);
                }
                if (noise2 > 0 && facet.getWorldArea().contains(position.x() + 1, position.y())) {
                    facet.setWorld(position.x() + 1, position.y(), true);
                }
                if (noise3 > 0 && facet.getWorldArea().contains(position.x() + 1, position.y() + 1)) {
                    facet.setWorld(position.x() + 1, position.y() + 1, true);
                }
                if (noise4 > 0 && facet.getWorldArea().contains(position.x(), position.y() + 1)) {
                    facet.setWorld(position.x(), position.y() + 1, true);
                }
//                //float spawnColumn = random.nextFloat(0, 1);
//                for (int l = 0; l <= x; l++) {
//                    for (int b = 0; b <= y; b++) {
//                        if (facet.getWorldRegion().contains(position.x() + l, position.y() + b)) {
//                            facet.setWorld(position.x() + l, position.y() + b, true);
//                        }
//                    }
//                }
            }
        }
        region.setRegionFacet(LavaFallsFacet.class, facet);
    }
}
