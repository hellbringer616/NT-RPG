/*
 *   Copyright (c) 2015, NeumimTo https://github.com/NeumimTo
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package cz.neumimto.rpg.configuration;

import cz.neumimto.rpg.damage.ItemDamageProcessor;
import cz.neumimto.rpg.inventory.ItemLoreSections;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by NeumimTo on 26.12.2014.
 */
@ConfigSerializable
public class PluginConfig {

	@Setting(comment= "If you want to use another plugin, which handles mob's hp and damage set this value to true")
	public boolean OVERRIDE_MOBS = false;

	@Setting
	public long COMBAT_TIME = 20000L;

	@Setting
	public boolean REMOVE_PLAYERDATA_AFTER_PERMABAN = false;

	@Setting(comment = "Ingame debug output. NONE - recommended level for your production server;" +
			" BALANCE - useful while you are balancing stuff it prints out incoming and outgoing damage, or you are setting up inventories," +
			" DEVELOP - very detailed logging, useful only for developers " +
			"!!!If you encounter any exceptions during server startup you will be asked to provide logs. For the developers are useful only logs "
			+ "created with DEBUG mode set to DEVELOP !!!")
	public DebugLevel DEBUG = DebugLevel.BALANCE;

	@Setting
	public int SKILLPOINTS_ON_START = 1;

	@Setting
	public boolean PLAYER_CAN_CHANGE_RACE = true;

	@Setting
	public boolean PLAYER_AUTO_CHOOSE_LAST_PLAYED_CHAR = true;

	@Setting
	public boolean SKILLGAIN_MESSAGES_AFTER_LOGIN = true;

	@Setting
	public boolean PLAYER_LEARNED_SKILL_GLOBAL_MESSAGE = true;

	@Setting
	public boolean PLAYER_UPGRADED_SKILL_GLOBAL_MESSAGE = true;

	@Setting
	public boolean PLAYER_REFUNDED_SKILL_GLOBAL_MESSAGE = true;

	@Setting
	public int ATTRIBUTEPOINTS_ON_START = 1;

	@Setting
	public int PLAYER_MAX_CHARS = 5;

	@Setting
	public boolean CAN_REFUND_SKILL = true;

	@Setting(comment = "Works only, if the server is using jdk, for passing these arguments with jre use -D flag")
	public String JJS_ARGS = "--optimistic-types=true";

	@Setting(comment = "Time period in milliseconds")
	public long MANA_REGENERATION_RATE = 1000;

	@Setting
	public boolean ALLOW_COMBAT_FOR_CHARACTERLESS_PLAYERS = true;

	@Setting(comment= "Works only if PLAYER_MAX_CHARS > 1.")
	public boolean TELEPORT_PLAYER_TO_LAST_CHAR_LOCATION = true;

	@Setting
	public Set<String> ALLOWED_RUNES_ITEMTYPES = new HashSet<String>() {{
		add("minecraft:nether_star");
	}};

	@Setting
	public boolean AUTOREMOVE_NONEXISTING_RUNEWORDS = false;

	@Setting(comment = "Enables passing arguments to skills\neg.: /skill Fireball arg1 arg2")
	public boolean SKILL_COMMAND_ARGUMENTS = false;

	@Setting
	(comment = "Multiplier of shared experience gain for players in a party.\nExp=(MobExp*Mult)/partyplayers in area")
	public double PARTY_EXPERIENCE_MULTIPLIER = 2;

	@Setting
	public double PARTY_EXPERIENCE_SHARE_DISTANCE = 25;

	@Setting
	(comment = "Value lesser than 0 means there will be no party limit. Skills or effects can override this value.")
	public double MAX_PARTY_SIZE = -68458;


	@Setting
	(comment = "If a player chooses a race and a class, where both those groups define damage value for one specific weapon, or "
			+ "projectile" +
			" this option specifies how the weapon damage will be calculated." +
			"1 = sum" +
			"2 = take highest value")
	public int WEAPON_MERGE_STRATEGY = 2;

	@Setting
	(comment = "Whenever global chat message will be displayed if any player chooses a skill tree path")
	public boolean PLAYER_CHOOSED_SKILLTREE_SPECIALIZATIon_GLOBAL_MESSAGE;

	@Setting
	(comment = "Whenever a player is able to refund skills, representing root of the path of specialization on any skilltree")
	public boolean PATH_NODES_SEALED = true;

	@Setting
	(comment = "Whenever pressing shift(sneak) resets click combination")
	public boolean SHIFT_CANCELS_COMBO = false;

	@Setting
	(comment = "Recognizes pressing Q key (/throwing an item out of inventory) as the click combo option 'Q'" +
			" This action has priority over throwing item out of the inventory. Click combo may not start whit this action")
	public boolean ENABLED_Q;

	@Setting
	(comment = "Recognizes pressing E key (/opening player inventory) as the click combo option 'E'" +
			" This action has priority over opening players' inventory. Click combo may not start whit this action")
	public boolean ENABLED_E;

	@Setting
	(comment = "Time interval in milliseconds, defines maximal interval between two clicks (E/Q/RMB/LMB/S)")
	public long CLICK_COMBO_MAX_INVERVAL_BETWEEN_ACTIONS = 1250;

	@Setting
	(comment = "If true then class is validated against character's race.allowedClasses parameter ")
	public boolean VALIDATE_RACE_DURING_CLASS_SELECTION = true;

	@Setting
	public String ITEM_LORE_EFFECT_NAME_COLOR = TextColors.BLUE.getName();

	@Setting
	public String ITEM_LORE_EFFECT_COLON_COLOR = TextColors.DARK_GRAY.getName();

	@Setting
	public String ITEM_LORE_EFFECT_VALUE_COLOR = TextColors.LIGHT_PURPLE.getName();

	@Setting
	public String ITEM_LORE_EFFECT_SECTION_COLOR = TextColors.BLUE.getName();

	@Setting
	public String ITEM_LORE_RARITY_COLOR = TextColors.DARK_GRAY.getName();


	@Setting
	public String ITEM_LORE_GROUP_MIN_LEVEL_COLOR = TextColors.DARK_PURPLE.getName();

	@Setting
	public List<String> ITEM_LORE_ORDER = Stream.of(ItemLoreSections.values()).map(ItemLoreSections::name).collect(Collectors.toList());


	@Setting
	public List<String> SKILLTREE_RELATIONS = new ArrayList<String>() {{
		add("|,minecraft:stick,|,0");
		add("/,minecraft:stick,/,0");
		add("\\\\,minecraft:stick,\\\\,0");
		add("-,minecraft:stick,-,0");
	}};

	@Setting
	public List<String> SKILLTREE_BUTTON_CONTROLLS = new ArrayList<String>() {{
		add("North,minecraft:diamond_hoe,Up,1");
		add("West,minecraft:diamond_hoe,Right,2");
		add("East,minecraft:diamond_hoe,Left,3");
		add("South,minecraft:diamond_hoe,Down,4");
	}};

	@Setting
	public List<String> ITEM_RARITY = new ArrayList<String>() {{
		add("0,Common");
		add("1,&9Rare");
		add("2,&eUnique");
		add("3,&5Legendary");
	}};

	@Setting
	public String EQUIPED_SLOT_RESOLVE_SRATEGY = "nt-rpg:persisted_slot_order";
/*
	@Setting
	@Comment(content = "1 - Drops item out of player inventory, 2 - Drops items only from hotbar, 3 - Only warning")
	public Integer PLAYER_IS_UNABLE_TO_USE_ITEM_RESOLVE_STRATEGY = 1;


	@Setting
	@Comment(content = "Player may put standard item into accessory slot")
	public boolean ACCESSORIES_BLOCK_JUNK_ITEMS = true;

	*/

	@Setting(comment = "Plugins attempt to create a list of items present on the server, which might fall into categories of weapons/armors/shields. "
					+ "The final list might, or might not be complete.")
	public boolean AUTODISCOVER_ITEMS = true;


	@Setting(comment = "If set to true plugin will generate first character right after log-in, for a players which do not have one yet. The new "
					+ "character has same name as the player. New players wont have to run command /char c playername<")
	public boolean CREATE_FIRST_CHAR_AFTER_LOGIN = true;

	@Setting
	public String LOCALE = "en";

	@Setting(comment = "A damage preprocessor which handles how is weapon damage calculated. Vanilla mc allows you to modify item damage by modifying generic.value nbt." +
			" CD = (Class Damage, calculated depending on WEAPON_MERGE_STRATEGY config node)" +
			"Possible Values: OVERRIDE = item nbt overrides CD; IGNORE = item nbt value will be ignored; SUM = nbt + CD; MAX = choses highest value either CD or nbt")
	public ItemDamageProcessor ITEM_DAMAGE_PROCESSOR = ItemDamageProcessor.IGNORE;
}
