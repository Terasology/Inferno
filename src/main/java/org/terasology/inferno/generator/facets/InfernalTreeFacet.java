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
package org.terasology.inferno.generator.facets;

import org.terasology.inferno.generator.structures.InfernalTree;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.base.SparseObjectFacet3D;

import java.util.ArrayList;
import java.util.List;

public class InfernalTreeFacet extends SparseObjectFacet3D<InfernalTree> {
    private List<Vector3i> treesPos = new ArrayList<Vector3i>();

    public InfernalTreeFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }

    public void addTree(Vector3i pos) {
        treesPos.add(pos);
    }

    public double getDistanceToNearestTree(Vector3i pos) {
        double distanceToNearest = -1f;
        for (Vector3i treePos: treesPos) {
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
