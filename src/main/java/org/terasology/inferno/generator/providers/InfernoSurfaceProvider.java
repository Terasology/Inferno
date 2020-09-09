// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inferno.generator.providers;

import org.terasology.engine.utilities.procedural.Noise;
import org.terasology.engine.utilities.procedural.SimplexNoise;
import org.terasology.engine.utilities.procedural.SubSampledNoise;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.inferno.generator.facets.InfernoSurfaceHeightFacet;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;

@Produces(InfernoSurfaceHeightFacet.class)
public class InfernoSurfaceProvider implements FacetProvider {
    private final int infernoDepth;
    private Noise surfaceNoise;

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

        Rect2i processRegion = facet.getWorldRegion();
        for (BaseVector2i position : processRegion.contents()) {
            facet.setWorld(position, surfaceNoise.noise(position.x(), position.y()) * 20 - infernoDepth);
        }
        region.setRegionFacet(InfernoSurfaceHeightFacet.class, facet);
    }
}
