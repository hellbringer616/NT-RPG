package cz.neumimto.rpg.sponge.skills.scripting;

import cz.neumimto.rpg.api.skills.mods.SkillContext;
import cz.neumimto.rpg.common.scripting.JsBinding;
import cz.neumimto.rpg.entities.IEntity;
import cz.neumimto.rpg.players.IActiveCharacter;

/**
 * Created by NeumimTo on 3.9.2018.
 */
@FunctionalInterface
@JsBinding(JsBinding.Type.CLASS)
public interface TargetedScriptExecutorSkill {

    void cast(IActiveCharacter character, IEntity target, SkillContext modifier, SkillScriptContext context);
}