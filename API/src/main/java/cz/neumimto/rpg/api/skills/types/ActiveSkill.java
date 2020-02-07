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

package cz.neumimto.rpg.api.skills.types;

import com.google.inject.Inject;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.inventory.InventoryService;
import cz.neumimto.rpg.api.localization.LocalizationKeys;
import cz.neumimto.rpg.api.skills.PlayerSkillContext;
import cz.neumimto.rpg.api.skills.SkillExecutionType;
import cz.neumimto.rpg.api.skills.SkillNodes;
import cz.neumimto.rpg.api.skills.SkillResult;
import cz.neumimto.rpg.api.skills.mods.SkillContext;
import cz.neumimto.rpg.api.skills.scripting.JsBinding;
import cz.neumimto.rpg.api.skills.tree.SkillType;

/**
 * Created by NeumimTo on 26.7.2015.
 */
@JsBinding(JsBinding.Type.CLASS)
public abstract class ActiveSkill<T extends IActiveCharacter> extends AbstractSkill implements IActiveSkill<T> {

    @Inject
    private InventoryService inventoryService;

    @Override
    public void init() {
        super.init();
        settings.addNode(SkillNodes.COOLDOWN, 10000, 0);
        settings.addNode(SkillNodes.HPCOST, 0, 0);
        settings.addNode(SkillNodes.MANACOST, 0, 0);
    }

    @Override
    public void onPreUse(IActiveCharacter character, SkillContext skillContext) {
        PlayerSkillContext info = character.getSkillInfo(this);

        if (character.isSilenced() && !getSkillTypes().contains(SkillType.CAN_CAST_WHILE_SILENCED)) {
            String translate = localizationService.translate(LocalizationKeys.PLAYER_SILENCED);
            character.sendMessage(translate);
            skillContext.result(SkillResult.CASTER_SILENCED);
            return;
        }
        skillContext.addExecutor(inventoryService.processItemCost(character, info));
        skillContext.addExecutor(info.getSkillData().getSkillPreprocessors());
        skillContext.sort();
        skillContext.next(character, info, skillContext);
    }

    public abstract void cast(T character, PlayerSkillContext info, SkillContext skillContext);


    public SkillContext createSkillExecutorContext(PlayerSkillContext esi) {
        return new SkillContext(this, esi);
    }

    @Override
    public SkillExecutionType getSkillExecutionType() {
        return SkillExecutionType.ACTIVE;
    }
}
