package cz.neumimto.rpg.skills.mods;

import static cz.neumimto.rpg.skills.mods.ModifierTargetExcution.AFTER;

import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.skills.ExtendedSkillInfo;
/**
 * Created by NeumimTo on 14.10.2018.
 */
public class SkillModifiers {

    public static SkillModifierProcessor MANA_BURN = new SkillModifierProcessor("mana_burn", AFTER) {

        @Override
        public void process(IActiveCharacter iActiveCharacter, SkillModifier copy, ExtendedSkillInfo info) {

        }
    };
}