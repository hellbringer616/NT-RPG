package cz.neumimto.rpg.spigot.commands;


import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import cz.neumimto.rpg.api.Rpg;
import cz.neumimto.rpg.api.damage.DamageService;
import cz.neumimto.rpg.api.effects.IGlobalEffect;
import cz.neumimto.rpg.api.entity.EntityService;
import cz.neumimto.rpg.api.entity.PropertyService;
import cz.neumimto.rpg.api.entity.players.CharacterService;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.entity.players.classes.ClassDefinition;
import cz.neumimto.rpg.api.entity.players.classes.PlayerClassData;
import cz.neumimto.rpg.api.items.ClassItem;
import cz.neumimto.rpg.api.items.ItemClass;
import cz.neumimto.rpg.api.items.RpgItemType;
import cz.neumimto.rpg.api.logging.Log;
import cz.neumimto.rpg.api.skills.ISkill;
import cz.neumimto.rpg.api.utils.ActionResult;
import cz.neumimto.rpg.common.commands.AdminCommandFacade;
import cz.neumimto.rpg.common.commands.CommandProcessingException;
import cz.neumimto.rpg.common.commands.InfoCommands;
import cz.neumimto.rpg.common.commands.OnlineOtherPlayer;
import cz.neumimto.rpg.spigot.entities.players.ISpigotCharacter;
import cz.neumimto.rpg.spigot.inventory.SpigotItemService;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Function;

@Singleton
@CommandAlias("nadmin|na")
@CommandPermission("ntrpg.admin")
public class SpigotAdminCommands {

    @Inject
    private AdminCommandFacade adminCommandFacade;

    @Inject
    private PropertyService propertyService;

    @Inject
    private EntityService entityService;

    @Inject
    private SpigotItemService itemService;

    @Inject
    private DamageService damageService;

    @Inject
    private SpigotCharacterCommands spigotCharacterCommands;

    @Inject
    private InfoCommands infoCommands;

    @Inject
    private CharacterService characterService;

    @Subcommand("class add")
    public void addCharacterClass(CommandSender commandSender, OnlineOtherPlayer player, ClassDefinition classDefinition) {
        IActiveCharacter character = player.character;
        ActionResult actionResult = adminCommandFacade.addCharacterClass(character, classDefinition);
        if (!actionResult.isOk()) {
            Log.error("Attempt to add player class safely failed, - class slot already occupied, player is lacking permission, missing prerequirements...");
        } else {
            Log.info("Player gained class via console " + character.getPlayerAccountName() + " class " + classDefinition.getName());
        }
    }

    @Subcommand("attributepoints add")
    @Description("Permanently adds X skillpoints to a player")
    public void addSkillPointsCommand(CommandSender commandSender, OnlineOtherPlayer player, @Default("1") int amount) {
        characterService.characterAddAttributePoints(player.character, amount);
    }

    @Subcommand("skillpoints add")
    @Description("Permanently adds X skillpoints to a player")
    public void addSkillPointsCommand(CommandSender commandSender, OnlineOtherPlayer player, ClassDefinition characterClass, @Default("1") int amount) {
        IActiveCharacter character = player.character;
        PlayerClassData classByName = character.getClassByName(characterClass.getName());
        if (classByName == null) {
            throw new CommandException("Player " + character.getPlayerAccountName() + " character " + character.getName() + " do not have class " + characterClass.getName());
        }
        characterService.characterAddSkillPoints(character,characterClass, amount);

    }

    @Subcommand("effect add")
    @Description("Adds effect, managed by rpg plugin, to the player")
    public void effectAddCommand(CommandSender commandSender, OnlineOtherPlayer player, IGlobalEffect effect, long duration, String[] args) {
        String data = String.join("", args);
        IActiveCharacter character = player.character;
        try {
            adminCommandFacade.commandAddEffectToPlayer(data, effect, duration, character);
        } catch (CommandProcessingException e) {
            commandSender.sendMessage(e.getMessage());
        }
    }


    @Subcommand("exp")
    @Description("Adds N experiences of given source type to a character")
    public void addExperiencesCommand(CommandSender executor, OnlineOtherPlayer target, double amount, String classOrSource) {
        try {
            adminCommandFacade.commandAddExperiences(target.character, amount, classOrSource);
        } catch (CommandProcessingException e) {
            executor.sendMessage(e.getMessage());
        }
    }

    @Subcommand("skill")
    @CommandCompletion("@skill=skill")
    public void adminExecuteSkillCommand(IActiveCharacter character, ISkill skill, @Optional String level) {
        adminCommandFacade.commandExecuteSkill(character, skill, level == null ? 1 : Integer.parseInt(level));
    }

    @Subcommand("char")
    @Description("forces opening of character menu on a client")
    public void adminExecuteSkillCommandAdmin(CommandSender console, OnlinePlayer executor) {
        spigotCharacterCommands.menu(executor.player);
    }

    @Subcommand("classes")
    public void showClassesCommandAdmin(CommandSender console, OnlineOtherPlayer executor, @Optional String type) {
        infoCommands.showClassesCommand(executor.character, type);
    }

    @Subcommand("class")
    @CommandPermission("ntrpg.info.class")
    public void showClassCommandAdmin(CommandSender console, OnlineOtherPlayer executor, ClassDefinition classDefinition, @Optional String back) {
        infoCommands.showClassCommand(executor.character, classDefinition, back);
    }


    @Subcommand("add-class")
    public void addClassToCharacterCommand(CommandSender executor, OnlineOtherPlayer target, ClassDefinition klass) {
        ActionResult actionResult = adminCommandFacade.addCharacterClass(target.character, klass);
        if (actionResult.isOk()) {
            executor.sendMessage(Rpg.get().getLocalizationService().translate("class.set.ok"));
        } else {
            executor.sendMessage(actionResult.getMessage());
        }
    }

    @Subcommand("add-unique-skillpoint")
    public void addUniqueSkillpoint(CommandSender executor, OnlineOtherPlayer target,  String classType, String sourceKey) {
        IActiveCharacter character = target.character;
        if (character.isStub()) {
            throw new IllegalStateException("Stub character");
        }
        adminCommandFacade.commandAddUniqueSkillpoint(character, classType, sourceKey);
    }

    @Subcommand("inspect property")
    public void inspectPropertyCommand(CommandSender executor, OnlinePlayer target, String property) {
        try {
            int idByName = propertyService.getIdByName(property);
            IActiveCharacter character = characterService.getCharacter(target.player.getUniqueId());
            executor.sendMessage(ChatColor.GOLD +"==================");
            executor.sendMessage(ChatColor.GREEN +  property);

            executor.sendMessage(ChatColor.GOLD + "Value" + ChatColor.WHITE + "/" +
                    ChatColor.AQUA + "Effective Value" + ChatColor.WHITE + "/" +
                    ChatColor.GRAY + "Cap" +
                    ChatColor.DARK_GRAY+ " .##");

            NumberFormat formatter = new DecimalFormat("#0.00");
            executor.sendMessage(ChatColor.GOLD + formatter.format(character.getProperty(idByName)) + ChatColor.WHITE + "/" +
                    ChatColor.AQUA + formatter.format(entityService.getEntityProperty(character, idByName)) + ChatColor.WHITE + "/" +
                    ChatColor.GRAY + formatter.format(propertyService.getMaxPropertyValue(idByName)));

            executor.sendMessage(ChatColor.GOLD + "==================");
            executor.sendMessage(ChatColor.GRAY + "Memory/1 player: " + (character.getPrimaryProperties().length * 2 * 4) / 1024.0 + "kb");

        } catch (Throwable t) {
            executor.sendMessage("No such property");
        }
    }

    @Subcommand("inspect item-damage")
    public void inspectItemDamageCommand(CommandSender executor, OnlinePlayer oplayer) {
        Player player = oplayer.player;
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack == null) {
            executor.sendMessage(player.getName() + " has no item in main hand");
            return;
        }

        java.util.Optional<RpgItemType> rpgItemType = itemService.getRpgItemType(itemStack);
        if (!rpgItemType.isPresent()) {
            executor.sendMessage(player.getName() + " has no Managed item in main hand");
            return;
        }
        RpgItemType fromItemStack = rpgItemType.get();
        ItemClass itemClass = fromItemStack.getItemClass();
        List<ItemClass> parents = new LinkedList<>();
        ItemClass parent = itemClass.getParent();
        List<Integer> o = new ArrayList<>();
        o.addAll(itemClass.getProperties());
        o.addAll(itemClass.getPropertiesMults());
        while (parent != null) {
            parents.add(parent);
            o.addAll(parent.getPropertiesMults());
            o.addAll(parent.getProperties());
            parent = parent.getParent();
        }
        parents.add(itemClass);
        Collections.reverse(parents);

        List<String> a = new ArrayList<>();
        for (ItemClass wc : parents) {
            a.addAll(TO_TEXT.apply(wc));
        }
        for (String text : a) {
            executor.sendMessage(text);
        }
        executor.sendMessage(ChatColor.GOLD + "==================");


        IActiveCharacter character = characterService.getCharacter(player.getUniqueId());
        executor.sendMessage(ChatColor.RED + "Damage: "+ damageService.getCharacterItemDamage(character, fromItemStack));
        executor.sendMessage(ChatColor.RED + "Details: ");
        executor.sendMessage(ChatColor.GRAY + " - From Item: " + character.getBaseWeaponDamage(fromItemStack));

        Collection<PlayerClassData> values = character.getClasses().values();
        for (PlayerClassData value : values) {
            Set<ClassItem> weapons = value.getClassDefinition().getWeapons();
            for (ClassItem weapon : weapons) {
                if (weapon.getType() == fromItemStack) {
                    executor.sendMessage(ChatColor.GRAY + "  - From Class: " + weapon.getDamage());
                }
            }
        }


        executor.sendMessage(ChatColor.GRAY + " - From ItemClass: ");
        Iterator<Integer> iterator = o.iterator();
        while (iterator.hasNext()) {
            int integer = iterator.next();
            String nameById = propertyService.getNameById(integer);

            if (nameById != null && !nameById.endsWith("_mult")) {
                iterator.remove();
            } else continue;

            executor.sendMessage(ChatColor.GRAY + "   - " + nameById + ":" + entityService.getEntityProperty(character, integer));
        }
        executor.sendMessage(ChatColor.GRAY + "   - Mult: ");
        iterator = o.iterator();
        while (iterator.hasNext()) {
            int integer = iterator.next();
            String nameById = propertyService.getNameById(integer);
            executor.sendMessage(ChatColor.GRAY + "   - " + nameById + ":" + entityService.getEntityProperty(character, integer));
        }
    }

    private Function<ItemClass, List<String>> TO_TEXT = weaponClass -> {
        List<String> list = new ArrayList<>();

        list.add(ChatColor.GOLD + weaponClass.getName());
        for (Integer property : weaponClass.getProperties()) {
            list.add(ChatColor.GRAY + " -> " + propertyService.getNameById(property));
        }
        for (Integer property : weaponClass.getPropertiesMults()) {
            list.add(ChatColor.GRAY + " -> " + propertyService.getNameById(property));
        }
        return list;
    };

}
