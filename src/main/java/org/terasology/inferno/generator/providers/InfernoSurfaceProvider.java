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
import org.joml.Vector2ic;
import org.terasology.inferno.generator.facets.InfernoSurfaceHeightFacet;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.SimplexNoise;
import org.terasology.utilities.procedural.SubSampledNoise;
import org.terasology.world.block.BlockAreac;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;

@Produces(InfernoSurfaceHeightFacet.class)
public class InfernoSurfaceProvider implements FacetProvider {
    private Noise surfaceNoise;
    private int infernoDepth;

    public InfernoSurfaceProvider(int depth) {
        this.infernoDepth = depth;
    }

    @Override
    public void setSeed(long seed) {
        // -1 to 1
        surfaceNoise = new SubSampledNoise(new SimplexNoise(seed), new Vector2f(0.007f, 0.007f), 1);
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(InfernoSurfaceHeightFacet.class);
        InfernoSurfaceHeightFacet facet = new InfernoSurfaceHeightFacet(region.getRegion(), border);
        facet.setBaseSurfaceHeight(infernoDepth);

        BlockAreac processRegion = facet.getWorldArea();
        for (Vector2ic position: processRegion) {
            facet.setWorld(position, surfaceNoise.noise(position.x(), position.y()) * 20 - infernoDepth);
        }
        region.setRegionFacet(InfernoSurfaceHeightFacet.class, facet);
    }
}
