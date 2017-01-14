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

package cz.neumimto.rpg.gui;

import cz.neumimto.core.ioc.Inject;
import cz.neumimto.core.ioc.IoC;
import cz.neumimto.core.ioc.Singleton;
import cz.neumimto.rpg.GroupService;
import cz.neumimto.rpg.NtRpgPlugin;
import cz.neumimto.rpg.ResourceLoader;
import cz.neumimto.rpg.commands.CommandChoose;
import cz.neumimto.rpg.commands.InfoCommand;
import cz.neumimto.rpg.configuration.Localization;
import cz.neumimto.rpg.configuration.PluginConfig;
import cz.neumimto.rpg.effects.EffectService;
import cz.neumimto.rpg.effects.EffectStatusType;
import cz.neumimto.rpg.effects.IEffect;
import cz.neumimto.rpg.effects.common.def.BossBarExpNotifier;
import cz.neumimto.rpg.inventory.data.InventoryItemMenuData;
import cz.neumimto.rpg.inventory.data.NKeys;
import cz.neumimto.rpg.inventory.runewords.RWService;
import cz.neumimto.rpg.inventory.runewords.Rune;
import cz.neumimto.rpg.persistance.DirectAccessDao;
import cz.neumimto.rpg.persistance.model.CharacterClass;
import cz.neumimto.rpg.players.CharacterBase;
import cz.neumimto.rpg.players.ExtendedNClass;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.players.groups.ConfigClass;
import cz.neumimto.rpg.players.groups.PlayerGroup;
import cz.neumimto.rpg.players.groups.Race;
import cz.neumimto.rpg.players.properties.attributes.ICharacterAttribute;
import cz.neumimto.rpg.skills.SkillData;
import cz.neumimto.rpg.skills.SkillTree;
import cz.neumimto.rpg.utils.ItemStackUtils;
import cz.neumimto.rpg.utils.Utils;
import cz.neumimto.rpg.utils.model.CharacterListModel;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.Color;
import cz.neumimto.rpg.gui.GuiHelper.*;

import java.util.*;
import java.util.stream.Collectors;

import static cz.neumimto.rpg.gui.GuiHelper.back;
import static cz.neumimto.rpg.gui.GuiHelper.createPlayerGroupView;
import static cz.neumimto.rpg.gui.GuiHelper.getItemLore;

/**
 * Created by NeumimTo on 6.8.2015.
 */
@Singleton
@ResourceLoader.ListenerClass
public class VanilaMessaging implements IPlayerMessage {

	@Inject
	private Game game;

	@Inject
	private GroupService groupService;

	@Inject
	private EffectService effectService;

	@Inject
	private NtRpgPlugin plugin;

	@Inject
	private RWService rwService;

	@Override
	public boolean isClientSideGui() {
		return false;
	}

	@Override
	public void invokerDefaultMenu(IActiveCharacter character) {

	}

	@Override
	public void sendMessage(IActiveCharacter player, String message) {
		player.sendMessage(message);
	}

	@Override
	public void sendCooldownMessage(IActiveCharacter player, String message, double cooldown) {
		sendMessage(player, Localization.ON_COOLDOWN.replaceAll("%1", message).replace("%2", String.valueOf(cooldown)));
	}

	@Override
	public void openSkillTreeMenu(IActiveCharacter player, SkillTree skillTree, SkillData skillData) {
		Sponge.getScheduler().createTaskBuilder().async().execute(() -> {

		}).submit(plugin);
	}

	@Override
	public void moveSkillTreeMenu(IActiveCharacter player, SkillTree skillTree, Map<String, Integer> learnedSkill, SkillData center) {

	}

	@Override
	public void sendEffectStatus(IActiveCharacter player, EffectStatusType type, IEffect effect) {
		sendMessage(player, type.toMessage(effect));
	}

	@Override
	public void invokeCharacterMenu(Player player, List<CharacterBase> characterBases) {
		ItemStack.Builder b = ItemStack.builder();
		List<ItemStack> list = new ArrayList<>();
		//todo
	}

	@Override
	public void sendManaStatus(IActiveCharacter character, double currentMana, double maxMana, double reserved) {
		Text.Builder b = Text.builder("Mana: " + currentMana).color(TextColors.BLUE);
		if (reserved != 0) {
			b.append(Text.builder(" / " + (maxMana - reserved)).color(TextColors.DARK_RED).build());
		}
		b.append(Text.builder(" | " + maxMana).color(TextColors.GRAY).build());
		character.getPlayer().sendMessage(b.build());
	}

	@Override
	public void sendPlayerInfo(IActiveCharacter character, List<CharacterBase> target) {
		PaginationService paginationService = game.getServiceManager().provide(PaginationService.class).get();
		PaginationList.Builder builder = paginationService.builder();
		builder.padding(Text.builder("=").color(TextColors.GREEN).build());
		List<Text> content = new ArrayList<>();
		for (CharacterBase characterBase : target) {
			String name = characterBase.getName();
			int level = character.getPrimaryClass().getLevel();
			String name1 = character.getRace().getName();
			Text.Builder b = Text.builder();
			b.append(Text.builder(" [").color(TextColors.DARK_GRAY).build())
					.append(Text.builder("SELECT").color(TextColors.GREEN).onClick(TextActions.runCommand("/" + "show" + " character " + name)).build())
					.append(Text.builder("] - ").color(TextColors.DARK_GRAY).build());
			b.append(Text.of(name)).append(Text.builder(" ").build()).append(Text.of(level));
			b.append(Text.of(name1));
			content.add(b.build());
		}
		builder.contents(content);
		builder.sendTo(character.getPlayer());
	}


	private String getDetailedCharInfo(IActiveCharacter character) {
		Text text = Text.builder("Level").color(TextColors.YELLOW).append(
				Text.builder("Race").color(TextColors.RED).append(
						Text.builder("Guild").color(TextColors.AQUA).append(
								Text.builder("Class").color(TextColors.GOLD).build()
						).build()).build()).build();
		return text.toString();
	}

	@Override
	public void sendPlayerInfo(IActiveCharacter character, IActiveCharacter target) {
		character.sendMessage(getDetailedCharInfo(target));
	}

	@Override
	public void showExpChange(IActiveCharacter character, String classname, double expchange) {
		IEffect effect = character.getEffect(BossBarExpNotifier.name);
		if (effect == null) {
			effect = new BossBarExpNotifier(character);
			effectService.addEffect(effect, character);
		}
		BossBarExpNotifier bossbar = (BossBarExpNotifier) effect;
		bossbar.setLevel(character.getPrimaryClass().getLevel());
		bossbar.notifyExpChange(classname, expchange);
	}

	@Override
	public void showLevelChange(IActiveCharacter character, ExtendedNClass clazz, int level) {
		Player player = character.getPlayer();
		player.sendMessage(Text.of("Level up: " + clazz.getConfigClass().getName() + " - " + level));
	}

	@Override
	public void sendStatus(IActiveCharacter character) {
		CharacterBase base = character.getCharacterBase();

		PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
		PaginationList.Builder builder = paginationService.builder();
		builder.title(Text.of(character.getName(), Color.YELLOW));
		builder.padding(Text.of("═", Color.GRAY));

		List<Text> content = new ArrayList<>();
		Set<CharacterClass> characterClasses = base.getCharacterClasses();
		for (CharacterClass cc : characterClasses) {
			Text t = Text.builder().append(Text.of(Utils.capitalizeFirst(cc.getName()), Color.GREEN))
					.append(Text.of(" - ", TextColors.GRAY))
					.append(Text.of(cc.getSkillPoints(), TextColors.BLUE))
					.append(Text.of(String.format("(%s)", cc.getUsedSkillPoints()), TextColors.GRAY))

					.toText();
			content.add(t);
		}
		content.add(Text.builder().append(Text.of("Attribute points: ", TextColors.GREEN))
				.append(Text.of(character.getCharacterBase().getAttributePoints(), TextColors.AQUA))
				.append(Text.of(String.format("(%s)", character.getCharacterBase().getUsedAttributePoints(), TextColors.GRAY))).toText());
		Player player = character.getPlayer();

		builder.contents(content);
		builder.sendTo(character.getPlayer());
	}



	@Override
	public void showClassInfo(IActiveCharacter character, ConfigClass cc) {

	}

	@Override
	public void sendListOfCharacters(final IActiveCharacter player, CharacterBase currentlyCreated) {
		PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
		PaginationList.Builder builder = paginationService.builder();
		Sponge.getScheduler().createTaskBuilder().async().execute(() -> {
			DirectAccessDao build = IoC.get().build(DirectAccessDao.class);
			String query = "select new cz.neumimto.rpg.utils.model.CharacterListModel(" +
					"c.name,d.name,d.experiences) " +
					"from CharacterBase c left join c.characterClasses d " +
					"where c.uuid = :id order by c.updated desc";
			Map map = new HashMap<>();
			map.put("id", player.getPlayer().getUniqueId());
			List<CharacterListModel> list = build.findList(CharacterListModel.class, query, map);
			List<Text> content = new ArrayList<Text>();
			builder.linesPerPage(10);
			builder.padding(Text.builder("=").color(TextColors.DARK_GRAY).build());
			GroupService s = IoC.get().build(GroupService.class);
			String current = player.getName();
			CommandChoose build1 = IoC.get().build(CommandChoose.class);
			String s1 = build1.getAliases().get(0);
			list.forEach(a -> {
				Text.Builder b = Text.builder(" -")
						.color(TextColors.GRAY);
				if (!a.getCharacterName().equalsIgnoreCase(current)) {
					b.append(Text.builder(" [").color(TextColors.DARK_GRAY).build())
							.append(Text.builder("SELECT").color(TextColors.GREEN).onClick(TextActions.runCommand("/" + s1 + " character " + a.getCharacterName())).build())
							.append(Text.builder("] - ").color(TextColors.DARK_GRAY).build());
				} else {
					b.append(Text.builder(" [").color(TextColors.DARK_GRAY).build())
							.append(Text.builder("*").color(TextColors.RED).build())
							.append(Text.builder("] - ").color(TextColors.DARK_GRAY).build());
				}
				b.append(Text.builder(a.getCharacterName()).color(TextColors.GRAY).append(Text.of(" ")).build());
				b.append(Text.builder(a.getPrimaryClassName()).color(TextColors.AQUA).append(Text.of(" ")).build());
				ConfigClass cc = s.getNClass(a.getPrimaryClassName());
				int level = 0;
				int m = 0;
				if (cc != ConfigClass.Default) {
					level = s.getLevel(cc, a.getPrimaryClassExp());
					m = cc.getMaxLevel();
				}
				b.append(Text.builder("Level: ").color(TextColors.DARK_GRAY).append(
						Text.builder(level + "").color(level == m ? TextColors.RED : TextColors.DARK_PURPLE).build()).build());
				content.add(b.build());
			});
			builder.title(Text.of("Characters", TextColors.WHITE))
					.contents(content);
			builder.sendTo(player.getEntity());

		}).submit(plugin);
	}

	@Override
	public void sendListOfRunes(IActiveCharacter character) {
		PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
		PaginationList.Builder builder = paginationService.builder();

		List<Text> content = new ArrayList<>();
		List<Rune> r = new ArrayList<>(rwService.getRunes().values());
		Collections.sort(r, (o1, o2) -> (int) (o1.getSpawnchance() - o2.getSpawnchance()));
		for (Rune rune : r) {
			content.add(Text.builder(rune.getName()).color(TextColors.GREEN).append(Text.builder(" - ").color(TextColors.WHITE).build()).append(rune.getLore()).color(TextColors.DARK_PURPLE).build());
		}
		builder.contents(content);
		builder.linesPerPage(10);
		builder.padding(Text.builder("=").color(TextColors.DARK_GRAY).build());
		builder.sendTo(character.getPlayer());


	}

	@Override
	public void showAvalaibleClasses(IActiveCharacter character) {
		displayCommonMenu(character, groupService.getClasses(), ConfigClass.Default);
	}

	@Override
	public void sendListOfRaces(IActiveCharacter character) {
		displayCommonMenu(character, groupService.getRaces(), Race.Default);
	}

	private void displayCommonMenu(IActiveCharacter character, Collection<? extends PlayerGroup> g, PlayerGroup default_) {
		Inventory i = Inventory.builder().of(InventoryArchetypes.DOUBLE_CHEST).build(plugin);
		for (PlayerGroup cc : g) {
			if (cc == default_) {
				continue;
			}
			if (!cc.isShowsInMenu() && !character.getPlayer().hasPermission("ntrpg.showsEverything")) {
				continue;
			}
			i.offer(createItemRepresentingGroup(cc));
		}
		character.getPlayer().openInventory(i, Cause.of(NamedCause.of(NtRpgPlugin.namedCause, plugin)));
	}

	private ItemStack createItemRepresentingGroup(PlayerGroup p) {
		String s1 = IoC.get().build(InfoCommand.class).getAliases().iterator().next();
		ItemStack s = ItemStack.of(p.getItemType(), 1);
		s.offer(NKeys.MENU_INVENTORY, true);
		s.offer(Keys.DISPLAY_NAME, Text.of(p.getName(), TextColors.DARK_PURPLE));
		s.offer(Keys.ITEM_LORE, getItemLore(p.getDescription()));
		String l = " race ";
		if (p instanceof ConfigClass) {
			l = " class ";
		}
		s.offer(new InventoryItemMenuData(s1 + l + p.getName()));
		return s;
	}

	@Override
	public void displayGroupArmor(PlayerGroup g, Player target) {
		Inventory i = Inventory.builder().of(InventoryArchetypes.DOUBLE_CHEST).build(plugin);
		List<List<ItemType>> rows = new ArrayList<>(5);
		for (int ki = 0; ki <= 5; ki++) {
			rows.add(new ArrayList<>());
		}
		for (ItemType type : g.getAllowedArmor()) {
			if (ItemStackUtils.isHelmet(type)) {
				rows.get(0).add(type);
			} else if (ItemStackUtils.isChestplate(type)) {
				rows.get(1).add(type);
			} else if (ItemStackUtils.isLeggings(type)) {
				rows.get(2).add(type);
			} else if (ItemStackUtils.isBoots(type)) {
				rows.get(3).add(type);
			} else {
				rows.get(4).add(type);
			}
		}
		ItemStack of = ItemStack.of(ItemTypes.PAPER, 1);
		String l = "class";
		if (g instanceof Race) {
			l = "race";
		}
		of.offer(NKeys.MENU_INVENTORY, true);
		of.offer(Keys.DISPLAY_NAME, Text.of(Localization.BACK, TextColors.WHITE));
		of.offer(new InventoryItemMenuData("show " + l + " " + g.getName()));
		i.query(new SlotPos(0, 0)).offer(of);

		int x = 2;
		int y = 0;
		for (List<ItemType> row : rows) {
			y = 0;
			for (ItemType type : row) {
				i.query(new SlotPos(x, y)).offer(ItemStack.of(type, 1));
				y++;
			}
			x++;
		}
		target.openInventory(i, Cause.of(NamedCause.of(NtRpgPlugin.namedCause, plugin)));
	}

	@Override
	public void displayGroupWeapon(PlayerGroup g, Player target) {
		Inventory i = Inventory.builder().of(InventoryArchetypes.DOUBLE_CHEST).build(plugin);
		List<List<ItemType>> rows = new ArrayList<>(5);
		for (int ki = 0; ki <= 5; ki++) {
			rows.add(new ArrayList<>());
		}

		ItemStack of = back(g);
		i.query(new SlotPos(0, 0)).offer(of);

		g.getWeapons().entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
				.collect(Collectors.toMap(Map.Entry::getKey,
						Map.Entry::getValue,
						(e1, e2) -> e1,
						LinkedHashMap::new)).forEach((type, aDouble) -> {
			ItemStack q = ItemStack.of(type, 1);
			q.offer(Keys.ITEM_LORE, Collections.singletonList(Text.of(TextColors.DARK_RED, TextStyles.BOLD, aDouble.toString())));
			q.offer(NKeys.MENU_INVENTORY, true);
			i.offer(q);
		});
		target.openInventory(i, Cause.of(NamedCause.of(NtRpgPlugin.namedCause, plugin)));
	}

	@Override
	public void sendRaceInfo(IActiveCharacter target, Race race) {
		Inventory i = createPlayerGroupView(race);
		if ((target.getRace() == null || target.getRace() == Race.Default) || PluginConfig.PLAYER_CAN_CHANGE_RACE) {
			ItemStack of = ItemStack.of(ItemTypes.DIAMOND, 1);
			of.offer(new InventoryItemMenuData("choose race " + race.getName()));
			of.offer(Keys.DISPLAY_NAME, Text.of(Localization.CONFIRM));
			i.query(new SlotPos(8,0)).offer(of);
		}
		target.getPlayer().openInventory(i, Cause.of(NamedCause.of(NtRpgPlugin.namedCause, plugin)));
	}

	@Override
	public void sendClassInfo(IActiveCharacter target, ConfigClass configClass) {
		Inventory i = createPlayerGroupView(configClass);
		target.getPlayer().openInventory(i, Cause.of(NamedCause.of(NtRpgPlugin.namedCause, plugin)));
	}

	@Override
	public void displayAttributes(Player player, PlayerGroup group) {
		Inventory i = Inventory.builder().of(InventoryArchetypes.DOUBLE_CHEST).build(plugin);
		i.query(new SlotPos(0, 0)).offer(back(group));

		int x = 1;
		int y = 1;
		for (Map.Entry<ICharacterAttribute, Integer> a : group.getStartingAttributes().entrySet()) {
			ICharacterAttribute key = a.getKey();
			Integer value = a.getValue();
			i.query(new SlotPos(x, y)).offer(createAttributeItem(key, value));
			//somehow format them in square-like structure
			if (x == 7) {
				x = 1;
				y ++;
			} else {
				x++;
			}
		}
		player.openInventory(i, Cause.of(NamedCause.of(NtRpgPlugin.namedCause, plugin)));
	}

	private ItemStack createAttributeItem(ICharacterAttribute key, Integer value) {
		ItemStack of = ItemStack.of(key.getItemRepresentation(), 1);
		of.offer(Keys.DISPLAY_NAME, Text.of(TextColors.DARK_RED, key.getName()));
		List<Text> lore = new ArrayList<>();
		of.offer(NKeys.MENU_INVENTORY, true);
		lore.add(Text.of(Localization.INITIAL_VALUE + ": " + value, TextColors.WHITE));
		lore.addAll(getItemLore(key.getDescription()));
		of.offer(Keys.ITEM_LORE, lore);
		return of;
	}

	@Listener
	public void onOptionSelect(ClickInventoryEvent event, @First(typeFilter = Player.class) Player player) {
		if (event.getTargetInventory().getArchetype() == InventoryArchetypes.CHEST ||
				event.getTargetInventory().getArchetype() == InventoryArchetypes.DOUBLE_CHEST) {
		//todo inventory.getPlugin
				List<SlotTransaction> transactions = event.getTransactions();

				if (transactions.size() == 1) {
					SlotTransaction t = transactions.get(0);
					Optional<String> s = t.getOriginal().get(NKeys.ANY_STRING);
					if (s.isPresent()) {
						if (event instanceof ClickInventoryEvent.Shift) {
							event.setCancelled(true);
							return;
						}
						player.closeInventory(Cause.of(NamedCause.of("player", player)));
						Sponge.getCommandManager().process(player, s.get());
					} else if (t.getOriginal().get(NKeys.MENU_INVENTORY).isPresent()) {
						event.setCancelled(true);
					}
				}
			}

	}


}
