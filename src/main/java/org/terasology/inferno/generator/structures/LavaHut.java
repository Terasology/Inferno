// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inferno.generator.structures;

import org.terasology.engine.math.Direction;

public class LavaHut {
    public int length;
    public int height = 4;
    public Direction hutDirection;

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getHeight() {
        return height;
    }

    public Direction getHutDirection() {
        return hutDirection;
    }

    public void setHutDirection(Direction dir) {
        this.hutDirection = dir;
    }

}
