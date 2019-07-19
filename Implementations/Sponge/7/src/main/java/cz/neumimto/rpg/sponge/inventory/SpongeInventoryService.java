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

package cz.neumimto.rpg.sponge.inventory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import cz.neumimto.config.blackjack.and.hookers.NotSoStupidObjectMapper;
import cz.neumimto.core.localization.TextHelper;
import cz.neumimto.rpg.api.IResourceLoader;
import cz.neumimto.rpg.api.Rpg;
import cz.neumimto.rpg.api.classes.ClassService;
import cz.neumimto.rpg.api.configuration.ItemString;
import cz.neumimto.rpg.api.configuration.SkillItemCost;
import cz.neumimto.rpg.api.effects.EffectParams;
import cz.neumimto.rpg.api.effects.IEffectService;
import cz.neumimto.rpg.api.entity.IPropertyService;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.entity.players.classes.ClassDefinition;
import cz.neumimto.rpg.api.inventory.CharacterInventoryInteractionHandler;
import cz.neumimto.rpg.api.inventory.ManagedSlot;
import cz.neumimto.rpg.api.inventory.RpgInventory;
import cz.neumimto.rpg.api.items.RpgItemStack;
import cz.neumimto.rpg.api.logging.Log;
import cz.neumimto.rpg.api.persistance.model.EquipedSlot;
import cz.neumimto.rpg.api.skills.ISkill;
import cz.neumimto.rpg.api.skills.PlayerSkillContext;
import cz.neumimto.rpg.api.skills.SkillCost;
import cz.neumimto.rpg.api.skills.SkillService;
import cz.neumimto.rpg.api.skills.mods.ActiveSkillPreProcessorWrapper;
import cz.neumimto.rpg.api.utils.Console;
import cz.neumimto.rpg.api.utils.Pair;
import cz.neumimto.rpg.common.inventory.AbstractInventoryService;
import cz.neumimto.rpg.common.inventory.ManagedInventory;
import cz.neumimto.rpg.common.inventory.SlotEffectSource;
import cz.neumimto.rpg.common.inventory.items.ItemMetaType;
import cz.neumimto.rpg.common.inventory.items.subtypes.ItemSubtype;
import cz.neumimto.rpg.common.inventory.items.subtypes.ItemSubtypes;
import cz.neumimto.rpg.sponge.NtRpgPlugin;
import cz.neumimto.rpg.sponge.damage.SpongeDamageService;
import cz.neumimto.rpg.sponge.entities.players.ISpongeCharacter;
import cz.neumimto.rpg.sponge.entities.players.SpongeCharacterService;
import cz.neumimto.rpg.sponge.gui.GuiConfig;
import cz.neumimto.rpg.sponge.gui.GuiDictionary;
import cz.neumimto.rpg.sponge.gui.ItemLoreBuilderService;
import cz.neumimto.rpg.sponge.inventory.data.NKeys;
import cz.neumimto.rpg.sponge.inventory.data.manipulators.*;
import cz.neumimto.rpg.sponge.inventory.runewords.RWService;
import cz.neumimto.rpg.sponge.persistance.EquipedSlotImpl;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static cz.neumimto.rpg.api.logging.Log.error;
import static cz.neumimto.rpg.sponge.NtRpgPlugin.pluginConfig;

/**
 * Created by NeumimTo on 22.7.2015.
 */
@Singleton
@IResourceLoader.ListenerClass
public class SpongeInventoryService extends AbstractInventoryService<ISpongeCharacter> {

    @Inject
    private SkillService skillService;

    @Inject
    private Game game;

    @Inject
    private SpongeCharacterService characterService;

    @Inject
    private IEffectService effectService;

    @Inject
    private SpongeDamageService spongeDamageService;

    @Inject
    private RWService rwService;

    @Inject
    private IPropertyService spongePropertyService;

    @Inject
    private CharacterInventoryInteractionHandler inventoryInteractionHandler;

    @Inject
    private ClassService classService;

    @Inject
    private NtRpgPlugin plugin;

    @Inject
    private SpongeItemService itemService;


    @Listener
    //Dump items once game started, so we can assume that registries wont change anymore
    public void dumpItems(GameStartedServerEvent event) {
        if (pluginConfig.AUTODISCOVER_ITEMS) {
            Collection<ItemType> allOf = Sponge.getRegistry().getAllOf(ItemType.class);
            ItemDumpConfig itemDump = new ItemDumpConfig();

            for (ItemType itemType : allOf) {
                String id = itemType.getId();
                Field[] fields = itemDump.getClass().getFields();
                for (Field field : fields) {
                    if (id.contains(field.getName())) {
                        try {
                            List<ItemType> itemTypes = (List<ItemType>) field.get(itemDump);
                            itemTypes.add(itemType);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                }
            }
            try {
                ObjectMapper.BoundInstance configMapper = ObjectMapper.forObject(itemDump);
                File properties = new File(NtRpgPlugin.workingDir, "itemDump.conf");
                if (properties.exists()) {
                    properties.delete();
                }
                HoconConfigurationLoader hcl = HoconConfigurationLoader.builder().setPath(properties.toPath()).build();
                SimpleConfigurationNode scn = SimpleConfigurationNode.root();
                configMapper.serialize(scn);
                hcl.save(scn);
            } catch (Exception e) {
                throw new RuntimeException("Could not write file. ", e);
            }


        }
    }


    @Override
    public void initializeCharacterInventory(ISpongeCharacter character) {
        if (inventoryInteractionHandler.handleInventoryInitializationPre(character)) {
            fillInventory(character);
            inventoryInteractionHandler.handleInventoryInitializationPost(character);
        }
    }

    private void fillInventory(ISpongeCharacter character) {
        Map<Class<?>, RpgInventory> managedInventory = character.getManagedInventory();
        Player player = character.getPlayer();
        for (Map.Entry<Class<?>, RpgInventory> e : managedInventory.entrySet()) {
            Class<?> key = e.getKey();
            Inventory query = player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of((Class<? extends Inventory>) key));
            RpgInventory rInv = e.getValue();
            for (Map.Entry<Integer, ManagedSlot> rInvE : rInv.getManagedSlots().entrySet()) {
                Inventory slot = query.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(rInvE.getKey())));
                Optional<ItemStack> content = slot.peek();
                if (content.isPresent()) {
                    Optional<RpgItemStack> rpgItemStack = itemService.getRpgItemStack(content.get());
                    if (rpgItemStack.isPresent()) {
                        rInvE.getValue().setContent(rpgItemStack.get());
                    }
                }
            }
        }
    }

    //todo event
    public void onRightClick(IActiveCharacter character, int slot, Slot hotbarSlot) {
        if (character.isStub()) {
            return;
        }

    }

    public void onLeftClick(IActiveCharacter character, int slot, Slot hotbarSlot) {
        if (character.isStub()) {
            return;
        }

    }

    public ItemStack setItemLevel(ItemStack itemStack, int level) {
        itemStack.offer(new ItemLevelData(level));
        return updateLore(itemStack);
    }

    public ItemStack updateLore(ItemStack is) {
        ItemLoreBuilderService.ItemLoreBuilder itemLoreBuilder = ItemLoreBuilderService.create(is, new ArrayList<>());
        is.offer(Keys.ITEM_LORE, itemLoreBuilder.buildLore());
        is.offer(Keys.HIDE_MISCELLANEOUS, true);
        is.offer(Keys.HIDE_ATTRIBUTES, true);
        return is;
    }

    public ItemStack addEffectsToItemStack(ItemStack is, String effectName, EffectParams effectParams) {
        EffectsData effectsData = is.getOrCreate(EffectsData.class).get();
        Optional<Map<String, EffectParams>> q = effectsData.get(NKeys.ITEM_EFFECTS);
        Map<String, EffectParams> w = q.orElse(new HashMap<>());
        w.put(effectName, effectParams);
        effectsData.set(NKeys.ITEM_EFFECTS, w);
        is.offer(effectsData);
        return is;
    }

    public void createItemMetaSectionIfMissing(ItemStack itemStack) {
        Optional<Text> text = itemStack.get(NKeys.ITEM_META_HEADER);
        if (!text.isPresent()) {
            itemStack.offer(new ItemMetaHeader(TextHelper.parse("&3Meta")));
        }
    }

    public void setItemRarity(ItemStack itemStack, Integer integer) {
        Optional<ItemRarityData> orCreate = itemStack.getOrCreate(ItemRarityData.class);
        ItemRarityData itemRarityData = orCreate.get();
        itemRarityData.set(NKeys.ITEM_RARITY, integer);
        itemStack.offer(itemRarityData);
    }

    public void createItemMeta(ItemStack itemStack, Text meta) {
        Optional<ItemMetaHeader> orCreate = itemStack.getOrCreate(ItemMetaHeader.class);
        ItemMetaHeader data = orCreate.get();
        data.set(NKeys.ITEM_META_HEADER, meta);
        itemStack.offer(data);
    }

    public void setItemRestrictions(ItemStack itemStack, Map<String, Integer> classReq, Map<String, Integer> attrreq) {
        Optional<MinimalItemRequirementsData> orCreate = itemStack.getOrCreate(MinimalItemRequirementsData.class);
        MinimalItemRequirementsData data = orCreate.get();
        data.set(NKeys.ITEM_ATTRIBUTE_REQUIREMENTS, attrreq);
        data.set(NKeys.ITEM_PLAYER_ALLOWED_GROUPS, classReq);
        itemStack.offer(data);
    }

    public void addGroupRestriction(ItemStack itemStack, ClassDefinition clazz, int level) {
        Optional<MinimalItemGroupRequirementsData> orCreate = itemStack.getOrCreate(MinimalItemGroupRequirementsData.class);
        MinimalItemGroupRequirementsData data = orCreate.get();
        Map<String, Integer> map = data.get(NKeys.ITEM_PLAYER_ALLOWED_GROUPS).orElse(new HashMap<>());
        map.put(clazz.getName(), level);
        data.set(NKeys.ITEM_PLAYER_ALLOWED_GROUPS, map);
        itemStack.offer(data);
    }

    public void setItemMetaType(ItemStack itemStack, ItemMetaType metaType) {
        ItemMetaTypeData orCreate = itemStack.getOrCreate(ItemMetaTypeData.class).orElse(new ItemMetaTypeData(metaType));
        orCreate.set(NKeys.ITEM_META_TYPE, metaType);
        itemStack.offer(orCreate);
    }

    public ItemStack createSkillbind(ISkill iSkill) {
        ItemStack itemStack = ItemStack.of(ItemTypes.PUMPKIN_SEEDS, 1);
        SkillBindData orCreate = itemStack.getOrCreate(SkillBindData.class).orElse(new SkillBindData(iSkill.getId()));
        orCreate.set(NKeys.SKILLBIND, iSkill.getId());
        itemStack.offer(Keys.DISPLAY_NAME, Text.of(iSkill.getLocalizableName()));
        itemStack.offer(orCreate);
        return itemStack;
    }


    @Override
    public void load() {
        Path path = Paths.get(Rpg.get().getWorkingDirectory(), "ItemGroups.conf");
        File f = path.toFile();
        if (!f.exists()) {
            Optional<Asset> asset = Sponge.getAssetManager().getAsset(NtRpgPlugin.GlobalScope.plugin, "ItemGroups.conf");
            if (!asset.isPresent()) {
                throw new IllegalStateException("Could not find an asset ItemGroups.conf");
            }
            try {
                asset.get().copyToFile(f.toPath());
            } catch (IOException e) {
                throw new IllegalStateException("Could not create ItemGroups.conf file", e);
            }
        }

        Config c = ConfigFactory.parseFile(path.toFile());

        List<? extends Config> inventorySlots = c.getConfigList("InventorySlots");
        for (Config inventorySlot : inventorySlots) {
            loadInventorySettings(inventorySlot);
        }

        List<String> itemMetaSubtypes = c.getStringList("ItemMetaSubtypes");

        itemMetaSubtypes.stream().map(ItemSubtype::new).forEach(a -> itemService.getItemSubtypes().get(a));

        loadSkillGuis();
    }

    private void loadSkillGuis() {
        Path path = Paths.get(Rpg.get().getWorkingDirectory(), "Gui.conf");
        File file = path.toFile();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.error("Could not create a file " + path.toString(), e);
            }
        }
        try {
            ObjectMapper<GuiConfig> mapper = NotSoStupidObjectMapper.forClass(GuiConfig.class);
            HoconConfigurationLoader hcl = HoconConfigurationLoader.builder().setPath(path).build();
            GuiConfig guiConfig = mapper.bind(new GuiConfig()).populate(hcl.load());
            guiConfig.getSkillIcons()
                    .entrySet()
                    .stream()
                    .filter(a -> skillService.getById(a.getKey()).isPresent())
                    .map(a -> new Pair<>(skillService.getById(a.getKey()).get(), a.getValue()))
                    .forEach(a -> GuiDictionary.addSkillIcon(a.key, a.value));
        } catch (ObjectMappingException | IOException e) {
            Log.error("Could not read " + path, e);
        }
    }


    private void loadInventorySettings(Config slots) {
        String aClass = slots.getString("type");
        try {
            Class<?> aClass1 = Class.forName(aClass);

            HashMap<Integer, SlotEffectSource> slotEffectSourceHashMap = new HashMap<>();
            ManagedInventory managedInventory = new ManagedInventory(aClass1, slotEffectSourceHashMap);
            for (String str : slots.getStringList("slots")) {
                String[] split = str.split(";");
                if (split.length == 1) {
                    SlotEffectSource slotEffectSource = new SlotEffectSource(Integer.parseInt(split[0]), ItemSubtypes.ANY);
                    slotEffectSourceHashMap.put(slotEffectSource.getSlotId(), slotEffectSource);
                } else {
                    ItemSubtype type = itemService.getItemSubtypes().get(split[1]);
                    if (type == null) {
                        type = ItemSubtypes.ANY;
                        error("Could not find subtype " + split[1]);
                    }
                    SlotEffectSource slotEffectSource = new SlotEffectSource(Integer.parseInt(split[0]), type);
                    slotEffectSourceHashMap.put(slotEffectSource.getSlotId(), slotEffectSource);
                }
            }
            managedInventories.put(managedInventory.getType(), managedInventory);
        } catch (ClassNotFoundException e) {
            error(Console.RED + "Could not find inventory type " + Console.GREEN + aClass + Console.RED
                    + " defined in ItemGroups.conf. Is the mod loaded? Is the class name correct? If you are unsure restart plugin with debug mode "
                    + "ON and interact with desired inventory");
        }
    }

    @Override
    public Set<ActiveSkillPreProcessorWrapper> processItemCost(ISpongeCharacter character, PlayerSkillContext skillInfo) {
        SkillCost invokeCost = skillInfo.getSkillData().getInvokeCost();
        if (invokeCost == null) {
            return Collections.emptySet();
        }
        Player player = character.getPlayer();
        Inventory query = player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class));
        Map<Inventory, Result> itemsToTake = new HashMap<>();
        int c = 0;
        outer:
        for (SkillItemCost skillItemCost : invokeCost.getItemCost()) {
            ItemString parsedItemType = skillItemCost.getItemType();
            int requiredAmount = skillItemCost.getAmount();
            for (Inventory inventory : query) {
                Optional<ItemStack> peek = inventory.peek();
                if (peek.isPresent()) {
                    ItemStack itemStack = peek.get();
                    ItemType itemType = Sponge.getRegistry().getType(ItemType.class, parsedItemType.itemId).get();
                    if (itemStack.getType() == itemType) {
                        if (itemStack.getQuantity() - requiredAmount < 0) {
                            itemsToTake.put(inventory, new Result(itemStack.getQuantity(), skillItemCost.consumeItems()));
                            requiredAmount -= itemStack.getQuantity();
                        } else {
                            itemsToTake.put(inventory, new Result(requiredAmount, skillItemCost.consumeItems()));
                            c++;
                            break outer;
                        }
                    }
                }
            }
        }
        if (c == invokeCost.getItemCost().size()) {
            for (Map.Entry<Inventory, Result> e : itemsToTake.entrySet()) {
                Result result = e.getValue();
                if (result.consume) {
                    Inventory slot = e.getKey();
                    slot.poll(result.amount);
                }
            }
        } else {
            return invokeCost.getInsufficientProcessors();
        }
        return Collections.emptySet();
    }

    @Override
    public EquipedSlot createEquipedSlot(String className, int slotId) {
        try {
            return new EquipedSlotImpl(className, slotId);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.error("Class not found - " + className, e);
        }
        return null;
    }

    public String getItemIconForSkill(ISkill iSkill) {
        return null;
    }
}