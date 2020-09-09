// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.caves;

import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.BaseBooleanFieldFacet3D;
import org.terasology.math.geom.Vector3i;

public class CaveFacet extends BaseBooleanFieldFacet3D {
    public CaveFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }

    public int getWorldIndex(Vector3i pos) {
        return getWorldIndex(pos.x, pos.y, pos.z);
    }
}
