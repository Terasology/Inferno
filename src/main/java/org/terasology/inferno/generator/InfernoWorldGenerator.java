// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inferno.generator;

import org.terasology.coreworlds.generator.facetProviders.BiomeProvider;
import org.terasology.coreworlds.generator.facetProviders.DefaultFloraProvider;
import org.terasology.coreworlds.generator.facetProviders.DefaultTreeProvider;
import org.terasology.coreworlds.generator.facetProviders.PerlinBaseSurfaceProvider;
import org.terasology.coreworlds.generator.facetProviders.PerlinHillsAndMountainsProvider;
import org.terasology.coreworlds.generator.facetProviders.PerlinHumidityProvider;
import org.terasology.coreworlds.generator.facetProviders.PerlinOceanProvider;
import org.terasology.coreworlds.generator.facetProviders.PerlinRiverProvider;
import org.terasology.coreworlds.generator.facetProviders.PerlinSurfaceTemperatureProvider;
import org.terasology.coreworlds.generator.facetProviders.PlateauProvider;
import org.terasology.coreworlds.generator.facetProviders.SeaLevelProvider;
import org.terasology.coreworlds.generator.facetProviders.SurfaceToDensityProvider;
import org.terasology.coreworlds.generator.rasterizers.FloraRasterizer;
import org.terasology.coreworlds.generator.rasterizers.SolidRasterizer;
import org.terasology.coreworlds.generator.rasterizers.TreeRasterizer;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.generation.BaseFacetedWorldGenerator;
import org.terasology.engine.world.generation.WorldBuilder;
import org.terasology.engine.world.generator.RegisterWorldGenerator;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.math.geom.ImmutableVector2i;

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
                .addPlugins();
    }
}
