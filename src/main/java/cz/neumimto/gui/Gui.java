/*    
 *     Copyright (c) 2015, NeumimTo https://github.com/NeumimTo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *     
 */

package cz.neumimto.gui;

import cz.neumimto.effects.EffectStatusType;
import cz.neumimto.effects.IEffect;
import cz.neumimto.core.ioc.IoC;
import cz.neumimto.players.CharacterBase;
import cz.neumimto.players.IActiveCharacter;
import cz.neumimto.skills.SkillInfo;
import cz.neumimto.skills.SkillTree;
import org.spongepowered.api.entity.living.player.Player;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by NeumimTo on 12.2.2015.
 */
public class Gui {

    public static IPlayerMessage vanilla;

  /*  public static IPlayerMessage mod;*/

    static {
        vanilla = IoC.get().build(VanilaMessaging.class);
       /* mod = IoC.get().build(MCGUIMessaging.class);*/
    }

    public static boolean isUsingClientSideGui(Player player) {
        return false;
    }

    public static IPlayerMessage getMessageTypeOf(IActiveCharacter player) {
       /* if (player.isUsingGuiMod())
            return mod;*/
        return vanilla;
    }

    public static IPlayerMessage getMessageTypeOf(Player player) {
   /*     if (isUsingClientSideGui(player))
            return mod;*/
        return vanilla;
    }

    public static void sendMessage(IActiveCharacter player, String message) {
        getMessageTypeOf(player).sendMessage(player, message);
    }

    public static void sendCooldownMessage(IActiveCharacter player, String skillname, long cooldown) {
        getMessageTypeOf(player).sendCooldownMessage(player, skillname, cooldown);
    }

    public static void openSkillTreeMenu(IActiveCharacter player, SkillTree skillTree, ConcurrentHashMap<String, Integer> learnedSkills) {
        getMessageTypeOf(player).openSkillTreeMenu(player, skillTree, learnedSkills);
    }

    public static void sendEffectStatus(IActiveCharacter player, EffectStatusType type, IEffect effect) {
        getMessageTypeOf(player).sendEffectStatus(player, type, effect);
    }

    public static void invokeCharacterMenu(Player player, List<CharacterBase> characterBases) {
        getMessageTypeOf(player).invokeCharacterMenu(player, characterBases);
    }

    public static void sendManaStatus(IActiveCharacter character, float currentMana, float maxMana, float reserved) {
        getMessageTypeOf(character).sendManaStatus(character, currentMana, maxMana, reserved);
    }

    public static void sendPlayerInfo(IActiveCharacter character, List<CharacterBase> target) {
        getMessageTypeOf(character).sendPlayerInfo(character, target);
    }

    public static void moveSkillTreeMenu(IActiveCharacter player, SkillTree skillTree, Map<String, Integer> learnedSkill, SkillInfo center) {
        getMessageTypeOf(player).moveSkillTreeMenu(player, skillTree, learnedSkill, center);
    }

}
