// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inferno.generator.providers;

import org.terasology.engine.utilities.procedural.BrownianNoise;
import org.terasology.engine.utilities.procedural.Noise;
import org.terasology.engine.utilities.procedural.SimplexNoise;
import org.terasology.engine.utilities.procedural.SubSampledNoise;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Updates;
import org.terasology.inferno.generator.facets.InfernoSurfaceHeightFacet;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;

@Updates(@Facet(InfernoSurfaceHeightFacet.class))
public class ElevationProvider implements FacetProvider {
    private static final float MAX_ELEVATION = 30;
    private Noise redNoise;

    @Override
    public void setSeed(long seed) {
        redNoise = new SubSampledNoise(new BrownianNoise(new SimplexNoise(seed + 10), 8), new Vector2f(0.001f,
                0.001f), 1);
    }

    @Override
    public void process(GeneratingRegion region) {
        InfernoSurfaceHeightFacet facet = region.getRegionFacet(InfernoSurfaceHeightFacet.class);
        // elevating ground to a height above the ceiling gives the appearance of walls
        Rect2i processRegion = facet.getWorldRegion();
        for (BaseVector2i position : processRegion.contents()) {
            float elevationHeight = redNoise.noise(position.x(), position.y()) * MAX_ELEVATION;
            elevationHeight = TeraMath.clamp(elevationHeight, 0, MAX_ELEVATION);
            facet.setWorld(position, facet.getWorld(position) + elevationHeight);
        }
    }
}
