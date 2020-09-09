// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inferno.generator.providers;

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
import org.terasology.math.geom.BaseVector2i;

@Produces(FloraFacet.class)
@Requires({@Facet(LavaLevelFacet.class), @Facet(InfernoSurfaceHeightFacet.class)})
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

        for (BaseVector2i position : surfaceHeightFacet.getWorldRegion().contents()) {
            int surfaceHeight = (int) Math.ceil(surfaceHeightFacet.getWorld(position));

            float n = noise.noise(position.x(), position.y());
            if (facet.getWorldRegion().encompasses(position.x(), surfaceHeight, position.y())
                    && surfaceHeight > lavaLevelFacet.getLavaLevel()
                    && noise.noise(position.x(), position.y()) > 0.96) {
                facet.setWorld(position.x(), surfaceHeight, position.y(), true);
            }
        }

        region.setRegionFacet(FloraFacet.class, facet);
    }
}
