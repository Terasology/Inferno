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
import org.terasology.engine.utilities.procedural.Noise;
import org.terasology.engine.utilities.procedural.WhiteNoise;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.inferno.generator.facets.FloraFacet;
import org.terasology.inferno.generator.facets.InfernoSurfaceHeightFacet;
import org.terasology.inferno.generator.facets.LavaLevelFacet;

@Produces(FloraFacet.class)
@Requires( {@Facet(LavaLevelFacet.class), @Facet(InfernoSurfaceHeightFacet.class)})
public class FloraProvider implements FacetProvider {
    private Noise noise;

    @Override
    public void setSeed(long seed) {
        noise = new WhiteNoise(seed + 3);
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(FloraFacet.class);
        FloraFacet facet = new FloraFacet(region.getRegion(), border);
        InfernoSurfaceHeightFacet surfaceHeightFacet = region.getRegionFacet(InfernoSurfaceHeightFacet.class);
        LavaLevelFacet lavaLevelFacet = region.getRegionFacet(LavaLevelFacet.class);

        for (Vector2ic position : surfaceHeightFacet.getWorldArea()) {
            int surfaceHeight = (int) Math.ceil(surfaceHeightFacet.getWorld(position));

            float n = noise.noise(position.x(), position.y());
            if (facet.getWorldRegion().contains(position.x(), surfaceHeight, position.y())
                    && surfaceHeight > lavaLevelFacet.getLavaLevel()
                    && noise.noise(position.x(), position.y()) > 0.96) {
                facet.setWorld(position.x(), surfaceHeight, position.y(), true);
            }
        }

        region.setRegionFacet(FloraFacet.class, facet);
    }
}
