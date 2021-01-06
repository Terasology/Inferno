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
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.utilities.procedural.BrownianNoise;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.SimplexNoise;
import org.terasology.utilities.procedural.SubSampledNoise;
import org.terasology.world.block.BlockArea;
import org.terasology.world.block.BlockAreac;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Updates;

@Updates(@Facet(InfernoSurfaceHeightFacet.class))
public class ElevationProvider implements FacetProvider {
    private static final float MAX_ELEVATION = 30;
    private Noise redNoise;

    @Override
    public void setSeed(long seed) {
        redNoise = new SubSampledNoise(new BrownianNoise(new SimplexNoise(seed + 10), 8), new Vector2f(0.001f, 0.001f), 1);
    }

    @Override
    public void process(GeneratingRegion region) {
        InfernoSurfaceHeightFacet facet = region.getRegionFacet(InfernoSurfaceHeightFacet.class);
        // elevating ground to a height above the ceiling gives the appearance of walls
        BlockAreac processRegion = facet.getWorldArea();
        for (Vector2ic position : processRegion) {
            float elevationHeight = redNoise.noise(position.x(), position.y()) * MAX_ELEVATION;
            elevationHeight = TeraMath.clamp(elevationHeight, 0, MAX_ELEVATION);
            facet.setWorld(position, facet.getWorld(position) + elevationHeight);
        }
    }
}
