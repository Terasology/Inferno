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

import org.terasology.inferno.generator.facets.InfernoSurfaceHeightFacet;
import org.terasology.inferno.generator.facets.LavaLevelFacet;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SeaLevelFacet;

@Produces(LavaLevelFacet.class)
@Requires(@Facet(InfernoSurfaceHeightFacet.class))
public class LavaLevelProvider implements FacetProvider {

    private int lavaLevel;

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
