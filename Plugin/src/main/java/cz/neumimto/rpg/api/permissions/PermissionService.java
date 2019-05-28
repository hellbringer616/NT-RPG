package cz.neumimto.rpg.api.permissions;

import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.players.PlayerClassData;

import java.util.Set;

public interface PermissionService {

    boolean hasPermission(IActiveCharacter character, String value);

    void removePermissions(IActiveCharacter character, Set<String> perms);

    void addPermissions(IActiveCharacter character, Set<String> perms);

    void addAllPermissions(IActiveCharacter character, PlayerClassData classDefinition);

    void addPermissions(IActiveCharacter character, PlayerClassData classDefinition);
}