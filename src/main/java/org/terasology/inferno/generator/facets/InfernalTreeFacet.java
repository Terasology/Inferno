// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inferno.generator.facets;

import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.SparseObjectFacet3D;
import org.terasology.inferno.generator.structures.InfernalTree;
import org.terasology.math.geom.Vector3i;

import java.util.ArrayList;
import java.util.List;

public class InfernalTreeFacet extends SparseObjectFacet3D<InfernalTree> {
    private final List<Vector3i> treesPos = new ArrayList<Vector3i>();

    public InfernalTreeFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }

    public void addTree(Vector3i pos) {
        treesPos.add(pos);
    }

    public double getDistanceToNearestTree(Vector3i pos) {
        double distanceToNearest = -1f;
        for (Vector3i treePos : treesPos) {
            if (treePos == pos) {
                continue;
            }
            if (treePos.distance(pos) < distanceToNearest || distanceToNearest < 0) {
                distanceToNearest = treePos.distance(pos);
            }
        }
        return distanceToNearest;
    }
}
