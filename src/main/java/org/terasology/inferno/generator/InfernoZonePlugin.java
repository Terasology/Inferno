/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.inferno.generator;

import org.terasology.caves.CaveFacetProvider;
import org.terasology.caves.CaveRasterizer;
import org.terasology.caves.CaveToDensityProvider;
import org.terasology.inferno.generator.providers.ElevationProvider;
import org.terasology.inferno.generator.providers.FloraProvider;
import org.terasology.inferno.generator.providers.InfernalTreeProvider;
import org.terasology.inferno.generator.providers.InfernoCeilingProvider;
import org.terasology.inferno.generator.providers.InfernoSurfaceProvider;
import org.terasology.inferno.generator.providers.LavaFallsProvider;
import org.terasology.inferno.generator.providers.LavaHutProvider;
import org.terasology.inferno.generator.providers.LavaLevelProvider;
import org.terasology.inferno.generator.rasterizers.InfernalTreeRasterizer;
import org.terasology.inferno.generator.rasterizers.InfernoFloraRasterizer;
import org.terasology.inferno.generator.rasterizers.InfernoWorldRasterizer;
import org.terasology.inferno.generator.rasterizers.LavaFallsRasterizer;
import org.terasology.inferno.generator.rasterizers.LavaHutRasterizer;
import org.terasology.world.generator.plugin.RegisterPlugin;
import org.terasology.world.zones.ConstantLayerThickness;
import org.terasology.world.zones.LayeredZoneRegionFunction;
import org.terasology.world.zones.ZonePlugin;

import static org.terasology.inferno.generator.InfernoWorldGenerator.INFERNO_DEPTH;
import static org.terasology.inferno.generator.InfernoWorldGenerator.INFERNO_HEIGHT;
import static org.terasology.world.zones.LayeredZoneRegionFunction.LayeredZoneOrdering.DEEP_UNDERGROUND;

@RegisterPlugin
public class InfernoZonePlugin extends ZonePlugin {

    public InfernoZonePlugin() {
        super("Inferno", new LayeredZoneRegionFunction(new ConstantLayerThickness(200_200), DEEP_UNDERGROUND));

        // Inferno
        addProvider(new InfernoSurfaceProvider(INFERNO_DEPTH));
        addProvider(new InfernoCeilingProvider(INFERNO_HEIGHT));
        addProvider(new ElevationProvider());
        addProvider(new LavaLevelProvider());
        addProvider(new LavaFallsProvider());
        addProvider(new FloraProvider());
        addProvider(new InfernalTreeProvider());
        addProvider(new LavaHutProvider());
        addRasterizer(new InfernoWorldRasterizer());
        // Caves rasterized right after main rasterizer
        addRasterizer(new CaveRasterizer());
        addRasterizer(new InfernoFloraRasterizer());
        addRasterizer(new LavaHutRasterizer());
        addRasterizer(new InfernalTreeRasterizer());
        addRasterizer(new LavaFallsRasterizer());
        // Caves
        addProvider(new CaveFacetProvider());
        addProvider(new CaveToDensityProvider());
    }
}



