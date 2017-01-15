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

import org.terasology.inferno.generator.facets.LavaFallsFacet;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;

@Produces(LavaFallsFacet.class)
public class LavaFallsProvider implements FacetProvider {

    private Noise spawnNoise;
    private Noise sizeNoise1;
    private Noise sizeNoise2;
    private Random random;

    @Override
    public void setSeed(long seed) {
        spawnNoise = new WhiteNoise(seed + 4);
        sizeNoise1 = new WhiteNoise(seed - 4);
        sizeNoise2 = new WhiteNoise(seed - 40);
        random = new FastRandom(seed + 4);
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(LavaFallsFacet.class).extendBy(4, 4, 4);
        LavaFallsFacet facet = new LavaFallsFacet(region.getRegion(), border);

        Rect2i processRegion = facet.getWorldRegion();
        for (BaseVector2i position : processRegion.contents()) {
            if (spawnNoise.noise(position.x(), position.y()) > 0.9993) {
                int x = Math.abs(Math.round(sizeNoise1.noise(position.x(), position.y())));
                int y = Math.abs(Math.round(sizeNoise2.noise(position.x(), position.y())));
                //float spawnColumn = random.nextFloat(0, 1);
                for (int l = 0; l <= x; l++) {
                    for (int b = 0; b <= y; b++) {
                        if (facet.getWorldRegion().contains(position.x() + l, position.y() + b)) {
                            facet.setWorld(position.x() + l, position.y() + b, true);
                        }
                    }
                }
            }
        }
        region.setRegionFacet(LavaFallsFacet.class, facet);
    }
}
