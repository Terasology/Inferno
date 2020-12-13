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
package org.terasology.caves;

import org.joml.Vector3i;
import org.terasology.world.block.BlockRegions;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.Updates;
import org.terasology.world.generation.facets.DensityFacet;

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

        for (Vector3i pos : BlockRegions.iterable(region.getRegion())) {
            if (caveFacet.getWorld(pos)) {
                densityFacet.setWorld(pos, -1f);
            }
        }
    }
}
