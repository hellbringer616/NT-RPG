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

package cz.neumimto.rpg.spigot.events.character;

import cz.neumimto.rpg.api.events.character.CharacterSkillRefundAttemptEvent;
import cz.neumimto.rpg.api.skills.ISkill;
import org.bukkit.event.HandlerList;

/**
 * Created by NeumimTo on 27.7.2015.
 */
public class SpigotCharacterSkillRefundAttemptEvent extends AbstractCharacterEvent implements CharacterSkillRefundAttemptEvent {

    private ISkill skill;
    private String failedTranslationKey;

    @Override
    public ISkill getSkill() {
        return skill;
    }

    @Override
    public void setSkill(ISkill skill) {
        this.skill = skill;
    }

    @Override
    public String getFailedTranslationKey() {
        return failedTranslationKey;
    }

    @Override
    public void setFailedTranslationKey(String failedTranslationKey) {
        this.failedTranslationKey = failedTranslationKey;
    }

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

}
