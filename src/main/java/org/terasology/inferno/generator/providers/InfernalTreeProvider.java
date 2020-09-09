// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inferno.generator.providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.utilities.procedural.BrownianNoise;
import org.terasology.engine.utilities.procedural.Noise;
import org.terasology.engine.utilities.procedural.SimplexNoise;
import org.terasology.engine.utilities.procedural.SubSampledNoise;
import org.terasology.engine.utilities.procedural.WhiteNoise;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetBorder;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.inferno.generator.facets.InfernalTreeFacet;
import org.terasology.inferno.generator.facets.InfernoCeilingHeightFacet;
import org.terasology.inferno.generator.facets.InfernoSurfaceHeightFacet;
import org.terasology.inferno.generator.structures.InfernalTree;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3i;

import java.util.ArrayList;
import java.util.List;

@Produces(InfernalTreeFacet.class)
@Requires({
        @Facet(value = InfernoCeilingHeightFacet.class, border = @FacetBorder(top = 25, bottom = 25, sides = 25)),
        @Facet(InfernoSurfaceHeightFacet.class)
})
public class InfernalTreeProvider implements FacetProvider {
    private static final int MIN_TRUNK_HEIGHT = 4;
    private static final int MAX_TRUNK_HEIGHT = 6;
    private static final int MIN_SPAWN_SPACE = 15;
    private final Logger logger = LoggerFactory.getLogger(InfernalTreeProvider.class);
    private Noise spawnNoise;
    private Noise heightNoise;
    private Random random;

    @Override
    public void setSeed(long seed) {
        spawnNoise = new WhiteNoise(seed + 20);
        heightNoise = new SubSampledNoise(new BrownianNoise(new SimplexNoise(seed + 30), 8), new Vector2f(0.001f,
                0.001f), 1);
        random = new FastRandom(seed + 20);
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(InfernalTreeFacet.class).extendBy(30, 30, 30);
        InfernalTreeFacet treeFacet = new InfernalTreeFacet(region.getRegion(), border);
        InfernoCeilingHeightFacet ceilingHeightFacet = region.getRegionFacet(InfernoCeilingHeightFacet.class);
        InfernoSurfaceHeightFacet surfaceHeightFacet = region.getRegionFacet(InfernoSurfaceHeightFacet.class);

        for (BaseVector2i position : ceilingHeightFacet.getWorldRegion().contents()) {
            int ceilingHeight = (int) Math.floor(ceilingHeightFacet.getWorld(position));
            float surfaceHeight = surfaceHeightFacet.getWorld(position);
            if (treeFacet.getWorldRegion().encompasses(position.x(), ceilingHeight, position.y())
                    && ceilingHeight - surfaceHeight > MIN_SPAWN_SPACE
                    && spawnNoise.noise(position.x(), position.y()) > 0.997) {
                int trunkHeight =
                        (int) TeraMath.clamp(heightNoise.noise(position.x(), position.y()) * MAX_TRUNK_HEIGHT,
                                MIN_TRUNK_HEIGHT, MAX_TRUNK_HEIGHT);
                Vector3i treePos = new Vector3i(position.x(), ceilingHeight, position.y());
                InfernalTree tree = new InfernalTree(trunkHeight, generateCanopy(trunkHeight));
                treeFacet.setWorld(treePos, tree);
                treeFacet.addTree(treePos);
            }
        }
        region.setRegionFacet(InfernalTreeFacet.class, treeFacet);
    }

    private List<Integer> generateCanopy(int trunkHeight) {
        List<Integer> canopySize = new ArrayList<>();
        int canopyStart = trunkHeight / 2 + 1;
        int canopyCount = trunkHeight - canopyStart + 1;
        for (int height = 0; height < trunkHeight + 1; height++) {
            if (height >= canopyStart) {
                canopySize.add(canopyCount);
                canopyCount--;
            } else {
                canopySize.add(0);
            }
        }
        return canopySize;
    }
}
