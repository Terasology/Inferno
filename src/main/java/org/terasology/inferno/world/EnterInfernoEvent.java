// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.inferno.world;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

public class EnterInfernoEvent implements Event {
    EntityRef clientEntity;

    public EnterInfernoEvent(EntityRef clientEntity) {
        this.clientEntity = clientEntity;
    }

    public EntityRef getClientEntity() {
        return clientEntity;
    }
}
