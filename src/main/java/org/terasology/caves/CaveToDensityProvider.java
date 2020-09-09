// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.caves;

import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Requires;
import org.terasology.engine.world.generation.Updates;
import org.terasology.engine.world.generation.facets.DensityFacet;
import org.terasology.math.geom.Vector3i;

@Updates(@Facet(DensityFacet.class))
@Requires(@Facet(CaveFacet.class))
public class CaveToDensityProvider implements FacetProvider {
    @Override
    public void setSeed(long seed) {
    }

    @Override
    public void process(GeneratingRegion region) {
        CaveFacet caveFacet = region.getRegionFacet(CaveFacet.class);
        DensityFacet densityFacet = region.getRegionFacet(DensityFacet.class);

        for (Vector3i pos : region.getRegion()) {
            if (caveFacet.getWorld(pos)) {
                densityFacet.setWorld(pos, -1f);
            }
        }
    }
}
