package cz.neumimto.rpg.common.inventory.crafting.runewords;

import cz.neumimto.rpg.sponge.inventory.sockets.SocketType;
import cz.neumimto.rpg.sponge.inventory.sockets.SocketTypes;

public class Gem extends ItemUpgrade {

    @Override
    public SocketType getSocketType() {
        return SocketTypes.GEM;
    }
}