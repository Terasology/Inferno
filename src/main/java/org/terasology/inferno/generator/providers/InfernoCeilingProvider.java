// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inferno.generator.providers;

import org.terasology.engine.utilities.procedural.Noise;
import org.terasology.engine.utilities.procedural.SimplexNoise;
import org.terasology.engine.utilities.procedural.SubSampledNoise;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.inferno.generator.facets.InfernoCeilingHeightFacet;
import org.terasology.inferno.generator.facets.InfernoSurfaceHeightFacet;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;

@Produces(InfernoCeilingHeightFacet.class)
@Requires(@Facet(InfernoSurfaceHeightFacet.class))
public class InfernoCeilingProvider implements FacetProvider {
    private final int infernoHeight;
    private Noise surfaceNoise;

    public InfernoCeilingProvider(int height) {
        this.infernoHeight = height;
    }

    @Override
    public void setSeed(long seed) {
        surfaceNoise = new SubSampledNoise(new SimplexNoise(seed + 1), new Vector2f(0.003f, 0.003f), 1);
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(InfernoCeilingHeightFacet.class);
        InfernoCeilingHeightFacet ceilingHeightFacet = new InfernoCeilingHeightFacet(region.getRegion(), border);
        InfernoSurfaceHeightFacet surfaceHeightFacet = region.getRegionFacet(InfernoSurfaceHeightFacet.class);
        int baseSurfaceHeight = surfaceHeightFacet.getBaseSurfaceHeight();

        Rect2i processRegion = ceilingHeightFacet.getWorldRegion();
        for (BaseVector2i position : processRegion.contents()) {
            ceilingHeightFacet.setWorld(position,
                    surfaceNoise.noise(position.x(), position.y()) * 20 - baseSurfaceHeight + infernoHeight);
        }

        region.setRegionFacet(InfernoCeilingHeightFacet.class, ceilingHeightFacet);
    }
}
