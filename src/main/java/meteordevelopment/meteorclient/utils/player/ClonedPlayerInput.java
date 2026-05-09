/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.player;

import net.minecraft.client.input.Input;
import net.minecraft.util.PlayerInput;

public class ClonedPlayerInput extends Input {

    public ClonedPlayerInput(Input other) {
        this.movementVector = other.getMovementInput();
        this.playerInput = new PlayerInput(
            other.playerInput.forward(),
            other.playerInput.backward(),
            other.playerInput.left(),
            other.playerInput.right(),
            other.playerInput.jump(),
            other.playerInput.sneak(),
            other.playerInput.sprint()
        );
    }

}
