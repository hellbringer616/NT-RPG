package cz.neumimto.rpg.common.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import cz.neumimto.rpg.api.entity.players.CharacterService;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.entity.players.classes.ClassDefinition;
import cz.neumimto.rpg.api.gui.Gui;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@CommandPermission("ntrpg.info")
@CommandAlias("show|ninfo")
public class InfoCommands extends BaseCommand {

    @Inject
    private CharacterService characterService;

    @Inject
    private CharacterCommandFacade characterCommandFacade;

    @CommandAlias("skilltree")
    public void openSkillTreeMenuCommand(IActiveCharacter character, @Optional ClassDefinition classDefinition) {
        characterCommandFacade.openSKillTreeMenu(character, classDefinition);
    }

    @Subcommand("classes")
    @CommandPermission("ntrpg.info.classes")
    public void showClassesCommand(IActiveCharacter character, @Optional String type) {
        if (type == null) {
            Gui.sendClassTypes(character);
        } else {
            Gui.sendClassesByType(character, type);
        }
    }

    @Subcommand("class")
    @CommandPermission("ntrpg.info.class")
    public void showClassCommand(IActiveCharacter character, ClassDefinition classDefinition, @Optional String back) {
        Gui.showClassInfo(character, classDefinition);
    }

    @Subcommand("character")
    @CommandPermission("ntrpg.info.player.characters.other")
    public void showOtherPlayerCharacterCommand(IActiveCharacter character, OnlineOtherPlayer target) {
        Gui.showCharacterInfo(character, target.character);
    }

    @Subcommand("character")
    @CommandPermission("ntrpg.info.player.characters.self")
    public void showPlayerCharacterCommand(IActiveCharacter character) {
        Gui.showCharacterInfo(character, character);
    }

    //todo remove select button
    @Subcommand("characters")
    public void showPlayerCharactersCommand(IActiveCharacter character) {
        Gui.sendListOfCharacters(character, character.getCharacterBase());
    }

    @Subcommand("class-weapons")
    public void showClassWeapons(IActiveCharacter character, ClassDefinition cc) {
        Gui.displayClassWeapons(cc, character);
    }

    @Subcommand("class-armor")
    public void showClassArmor(IActiveCharacter character, ClassDefinition cc) {
        Gui.displayClassArmor(cc, character);
    }

//    @Subcommand("runeword")
//    @CommandPermission("ntrpg.info.player.characters.other")
//    public void showRunewordInfoCommand(IActiveCharacter character, RuneWord runeword) {
//        messaging.displayRuneword(character, runeword, true);
//    }
//
//    @Subcommand("runeword allowed-items")
//    public void displayRunewordAllowedItemsCommand(IActiveCharacter character, RuneWord runeWord) {
//        messaging.displayRunewordAllowedItems(character, runeWord);
//    }
//
//    @Subcommand("runeword allowed-items")
//    public void displayRunewordAllowedClassesCommand(IActiveCharacter character, RuneWord runeWord) {
//        messaging.displayRunewordAllowedGroups(character, runeWord);
//    }
//
//    @Subcommand("runeword required-classes")
//    public void displayRunewordRequiredClassesCommand(IActiveCharacter character, RuneWord runeWord) {
//        messaging.displayRunewordRequiredGroups(character, runeWord);
//    }
//
//    @Subcommand("runeword blocked-classes")
//    public void displayRunewordBlockedClassesCommand(IActiveCharacter character, RuneWord runeWord) {
//        messaging.displayRunewordBlockedGroups(character, runeWord);
//    }

    @Subcommand("attributes-initial")
    public void displayInitialClassAttributesCommand(IActiveCharacter character, ClassDefinition classDefinition) {
        Gui.displayInitialAttributes(classDefinition, character);
    }

    @Subcommand("properties-initial")
    public void displayInitialClassPropertiesCommand(IActiveCharacter character, ClassDefinition classDefinition) {
        Gui.displayInitialProperties(classDefinition, character);
    }

    @Subcommand("stats")
    public void displayCharacterStatsCommand(IActiveCharacter character) {
        Gui.sendStatus(character);
    }
}
