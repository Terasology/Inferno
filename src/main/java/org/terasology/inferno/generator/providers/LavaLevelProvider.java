// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inferno.generator.providers;

import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.inferno.generator.facets.InfernoSurfaceHeightFacet;
import org.terasology.inferno.generator.facets.LavaLevelFacet;

@Produces(LavaLevelFacet.class)
@Requires(@Facet(InfernoSurfaceHeightFacet.class))
public class LavaLevelProvider implements FacetProvider {

    private final int lavaLevel;

    public LavaLevelProvider() {
        lavaLevel = 0;
    }

    public LavaLevelProvider(int lavaLevel) {
        this.lavaLevel = lavaLevel;
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(LavaLevelFacet.class);
        LavaLevelFacet facet = new LavaLevelFacet(region.getRegion(), border);
        InfernoSurfaceHeightFacet surfaceHeightFacet = region.getRegionFacet(InfernoSurfaceHeightFacet.class);
        facet.setLavaLevel(lavaLevel - surfaceHeightFacet.getBaseSurfaceHeight());
        region.setRegionFacet(LavaLevelFacet.class, facet);
    }
}
