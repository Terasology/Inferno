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
package org.terasology.inferno.generator;

import org.terasology.caves.CaveFacetProvider;
import org.terasology.caves.CaveRasterizer;
import org.terasology.caves.CaveToDensityProvider;
import org.terasology.core.world.generator.facetProviders.BiomeProvider;
import org.terasology.core.world.generator.facetProviders.DefaultFloraProvider;
import org.terasology.core.world.generator.facetProviders.DefaultTreeProvider;
import org.terasology.core.world.generator.facetProviders.PerlinBaseSurfaceProvider;
import org.terasology.core.world.generator.facetProviders.PerlinHillsAndMountainsProvider;
import org.terasology.core.world.generator.facetProviders.PerlinHumidityProvider;
import org.terasology.core.world.generator.facetProviders.PerlinOceanProvider;
import org.terasology.core.world.generator.facetProviders.PerlinRiverProvider;
import org.terasology.core.world.generator.facetProviders.PerlinSurfaceTemperatureProvider;
import org.terasology.core.world.generator.facetProviders.PlateauProvider;
import org.terasology.core.world.generator.facetProviders.SeaLevelProvider;
import org.terasology.core.world.generator.facetProviders.SurfaceToDensityProvider;
import org.terasology.core.world.generator.rasterizers.FloraRasterizer;
import org.terasology.core.world.generator.rasterizers.SolidRasterizer;
import org.terasology.core.world.generator.rasterizers.TreeRasterizer;
import org.terasology.engine.SimpleUri;
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
import org.terasology.math.geom.ImmutableVector2i;
import org.terasology.registry.In;
import org.terasology.world.generation.BaseFacetedWorldGenerator;
import org.terasology.world.generation.WorldBuilder;
import org.terasology.world.generator.RegisterWorldGenerator;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;

@RegisterWorldGenerator(id = "infernoWorld", displayName = "Inferno")
public class InfernoWorldGenerator extends BaseFacetedWorldGenerator {
    public static final int INFERNO_DEPTH = 100000;
    public static final int INFERNO_HEIGHT = 35;
    public static final int INFERNO_BORDER = 20000;

    @In
    private WorldGeneratorPluginLibrary worldGeneratorPluginLibrary;

    public InfernoWorldGenerator(SimpleUri uri) {
        super(uri);
    }

    @Override
    protected WorldBuilder createWorld() {
        int seaLevel = 32;
        ImmutableVector2i spawnPos = new ImmutableVector2i(0, 0); // as used by the spawner

        return new WorldBuilder(worldGeneratorPluginLibrary)
                // default Perlin
                .setSeaLevel(seaLevel)
                .addProvider(new SeaLevelProvider(seaLevel))
                .addProvider(new PerlinHumidityProvider())
                .addProvider(new PerlinSurfaceTemperatureProvider())
                .addProvider(new PerlinBaseSurfaceProvider())
                .addProvider(new PerlinRiverProvider())
                .addProvider(new PerlinOceanProvider())
                .addProvider(new PerlinHillsAndMountainsProvider())
                .addProvider(new BiomeProvider())
                .addProvider(new SurfaceToDensityProvider())
                .addProvider(new DefaultFloraProvider())
                .addProvider(new DefaultTreeProvider())
                .addProvider(new PlateauProvider(spawnPos, seaLevel + 4, 10, 30))
                .addRasterizer(new SolidRasterizer())
                .addRasterizer(new FloraRasterizer())
                .addRasterizer(new TreeRasterizer())
                // Inferno
                .addProvider(new InfernoSurfaceProvider(INFERNO_DEPTH))
                .addProvider(new InfernoCeilingProvider(INFERNO_HEIGHT))
                .addProvider(new ElevationProvider())
                .addProvider(new LavaLevelProvider())
                .addProvider(new LavaFallsProvider())
                .addProvider(new FloraProvider())
                .addProvider(new InfernalTreeProvider())
                .addProvider(new LavaHutProvider())
                .addRasterizer(new InfernoWorldRasterizer())
                // Caves rasterized right after main rasterizer
                .addRasterizer(new CaveRasterizer())
                .addRasterizer(new InfernoFloraRasterizer())
                .addRasterizer(new LavaHutRasterizer())
                .addRasterizer(new InfernalTreeRasterizer())
                .addRasterizer(new LavaFallsRasterizer())
                // Caves
                .addProvider(new CaveFacetProvider())
                .addProvider(new CaveToDensityProvider());
    }
}
