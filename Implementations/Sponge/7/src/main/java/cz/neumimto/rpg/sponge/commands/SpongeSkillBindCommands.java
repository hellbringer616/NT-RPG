package cz.neumimto.rpg.sponge.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.localization.LocalizationKeys;
import cz.neumimto.rpg.api.localization.LocalizationService;
import cz.neumimto.rpg.api.skills.ISkill;
import cz.neumimto.rpg.api.skills.PlayerSkillContext;
import cz.neumimto.rpg.api.skills.types.ActiveSkill;
import cz.neumimto.rpg.sponge.entities.players.SpongeCharacterService;
import cz.neumimto.rpg.sponge.inventory.SpongeInventoryService;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@CommandAlias("bind")
@CommandPermission("ntrpg.player.skillbind")
public class SpongeSkillBindCommands extends BaseCommand {

    @Inject
    private LocalizationService localizationService;

    @Inject
    private SpongeCharacterService characterService;

    @Inject
    private SpongeInventoryService inventoryService;

    @Default
    public void bindSkillCommand(IActiveCharacter executor, ISkill skill) {
        if (!(skill instanceof ActiveSkill)) {
            String msg = localizationService.translate(LocalizationKeys.CANNOT_BIND_NON_EXECUTABLE_SKILL);
            executor.sendMessage(msg);
            return;
        }
        PlayerSkillContext info = executor.getSkillInfo(skill);
        ItemStack is = inventoryService.createSkillbind(info);
        ((Player) executor.getEntity()).getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class)).offer(is);
    }
}
