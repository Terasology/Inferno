// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inferno.generator.providers;

import org.terasology.engine.utilities.procedural.Noise;
import org.terasology.engine.utilities.procedural.WhiteNoise;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;

public class LavaFallsSpreadProvider implements FacetProvider {
    private Noise noise;
    private Random random;

    @Override
    public void setSeed(long seed) {
        noise = new WhiteNoise(seed + 5);
        random = new FastRandom(seed + 5);
    }

    @Override
    public void process(GeneratingRegion region) {
    }
}
