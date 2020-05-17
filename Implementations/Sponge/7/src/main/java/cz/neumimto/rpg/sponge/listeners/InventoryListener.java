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

package cz.neumimto.rpg.sponge.listeners;

import com.google.inject.Singleton;
import cz.neumimto.rpg.api.ResourceLoader;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.inventory.InventoryService;
import cz.neumimto.rpg.api.inventory.ManagedSlot;
import cz.neumimto.rpg.api.inventory.RpgInventory;
import cz.neumimto.rpg.api.items.ItemClass;
import cz.neumimto.rpg.api.items.RpgItemStack;
import cz.neumimto.rpg.api.localization.LocalizationKeys;
import cz.neumimto.rpg.api.localization.LocalizationService;
import cz.neumimto.rpg.api.skills.PlayerSkillContext;
import cz.neumimto.rpg.api.skills.SkillService;
import cz.neumimto.rpg.common.inventory.InventoryHandler;
import cz.neumimto.rpg.sponge.entities.players.ISpongeCharacter;
import cz.neumimto.rpg.sponge.entities.players.SpongeCharacterService;
import cz.neumimto.rpg.sponge.inventory.SpongeItemService;
import cz.neumimto.rpg.sponge.inventory.data.NKeys;
import cz.neumimto.rpg.sponge.inventory.data.manipulators.SkillBindData;
import cz.neumimto.rpg.sponge.utils.ItemStackUtils;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.ChangeEntityEquipmentEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.filter.type.Include;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Tristate;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Created by NeumimTo on 22.7.2015.
 */
@Singleton
@ResourceLoader.ListenerClass
public class InventoryListener {

    private final static int OFFHAND_SLOT_ID = 40;

    @Inject
    private SpongeCharacterService characterService;
    @Inject
    private InventoryHandler inventoryHandler;
    @Inject
    private InventoryService inventoryService;
    @Inject
    private SpongeItemService itemService;
    @Inject
    private LocalizationService localizationService;
    @Inject
    private SkillService skillService;

    @Listener
    @IsCancelled(Tristate.FALSE)
    public void onItemPickup(ChangeInventoryEvent.Pickup event, @Root Player player) {
        ISpongeCharacter character = characterService.getCharacter(player);
        if (character.isStub()) {
            return;
        }

        Inventory targetInventory = player.getInventory();

        SlotTransaction slotTransaction = event.getTransactions().get(0);
        Map<Class<?>, RpgInventory> managedInventory = character.getManagedInventory();
        RpgInventory rpgInventory = managedInventory.get(targetInventory.getClass());

        if (rpgInventory != null) {
            Slot slot = slotTransaction.getSlot();
            Optional<SlotIndex> inventoryProperty = slot.transform().getInventoryProperty(SlotIndex.class);
            SlotIndex slotIndex = inventoryProperty.get();
            Integer value = slotIndex.getValue();
            if (rpgInventory.getManagedSlots().containsKey(value)) {

                Optional<RpgItemStack> rpgItemStack = itemService.getRpgItemStack(slotTransaction.getFinal().createStack());
                rpgItemStack.ifPresent(itemStack -> {
                    ManagedSlot managedSlot = rpgInventory.getManagedSlots().get(value);
                    if (inventoryHandler.handleCharacterEquipActionPre(character, managedSlot, itemStack)) {
                        inventoryHandler.handleCharacterEquipActionPost(character, managedSlot, itemStack);
                        character.setRequiresDamageRecalculation(true);
                    } else {

                    }
                });
            }
        }
    }

    @Listener
    @IsCancelled(Tristate.FALSE)
    public void onItemDrop(DropItemEvent.Pre event, @Root Player player) {
        ISpongeCharacter character = characterService.getCharacter(player);
        if (character.isStub()) {
            return;
        }

        if (!player.getOpenInventory().isPresent()) {
            return;
        }

        CarriedInventory<? extends Carrier> inventory = player.getInventory();
        Hotbar query = inventory.query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class));
        int selectedSlotIndex = query.getSelectedSlotIndex();
        Map<Class<?>, RpgInventory> managedInventory = character.getManagedInventory();
        RpgInventory rpgInventory = managedInventory.get(inventory.getClass());
        if (rpgInventory.getManagedSlots().containsKey(selectedSlotIndex)) {
            ManagedSlot currentHand = rpgInventory.getManagedSlots().get(selectedSlotIndex);
            Optional<RpgItemStack> content = currentHand.getContent();
            content.ifPresent(i -> {
                inventoryHandler.handleCharacterUnEquipActionPost(character, currentHand);
                character.setRequiresDamageRecalculation(true);
                character.setMainHand(null, -1);
            });
        }
    }


    @Listener
    public void onHotbarInteract(HandInteractEvent event, @First Player player) {
        ISpongeCharacter character = characterService.getCharacter(player);
        if (character.isStub()) {
            return;
        }

        CarriedInventory<? extends Carrier> inventory = player.getInventory();

        Hotbar query = inventory.query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class));
        int selectedSlotIndex = query.getSelectedSlotIndex();

        Optional<ItemStack> itemInHand = player.getItemInHand(HandTypes.MAIN_HAND);
        if (itemInHand.isPresent()) {
            ItemStack itemStack = itemInHand.get();
            Optional<RpgItemStack> rpgItemType = itemService.getRpgItemStack(itemStack);
            if (rpgItemType.isPresent()) {
                RpgItemStack rpgItemType1 = rpgItemType.get();

                int last = character.getLastHotbarSlotInteraction();
                if (selectedSlotIndex != last) {
                    Map<Class<?>, RpgInventory> managedInventory = character.getManagedInventory();
                    Map<Integer, ManagedSlot> managedSlots = managedInventory.get(inventory.getClass()).getManagedSlots();

                    if (managedSlots.containsKey(selectedSlotIndex)) {
                        ManagedSlot managedSlot = managedSlots.get(selectedSlotIndex);
                        if (inventoryHandler.handleCharacterEquipActionPre(character, managedSlot, rpgItemType1)) {
                            inventoryHandler.handleInventoryInitializationPost(character);
                            character.setLastHotbarSlotInteraction(selectedSlotIndex);
                            character.setMainHand(rpgItemType1, selectedSlotIndex);
                        } else {
                            ItemStackUtils.dropItem(player, itemStack);
                            player.setItemInHand(HandTypes.MAIN_HAND, ItemStack.empty());
                            character.setLastHotbarSlotInteraction(-1);
                            String translate = localizationService.translate(LocalizationKeys.CANNOT_USE_ITEM_CONFIGURATION_REASON);
                            player.sendMessage(ChatTypes.ACTION_BAR, Text.of(TextColors.RED, translate));
                            event.setCancelled(true);
                            character.setRequiresDamageRecalculation(true);
                        }
                    }
                }
            } else {
                character.setMainHand(null, -1);
                character.setLastHotbarSlotInteraction(-1);
                character.setRequiresDamageRecalculation(true);
            }
        }
    }

    @Listener
    @Include({
            ClickInventoryEvent.Primary.class,
            ClickInventoryEvent.Secondary.class,
            ClickInventoryEvent.Drag.class,
            ClickInventoryEvent.Shift.class
    })
    public void onInteract(ClickInventoryEvent event, @Root Player player) {
        final List<SlotTransaction> transactions = event.getTransactions();
        for (SlotTransaction slotTransaction : transactions) {
            Slot slot = slotTransaction.getSlot();
            Slot transformed = slot.transform();
            Class aClass = transformed.parent().getClass();
            int slotId = transformed.getInventoryProperty(SlotIndex.class).get().getValue();
            if (!inventoryService.isManagedInventory(aClass, slotId)) {
                return;
            }
            IActiveCharacter character = characterService.getCharacter(player);
            Map<Class<?>, RpgInventory> managedInventory = character.getManagedInventory();
            RpgInventory rpgInventory = managedInventory.get(aClass);
            ManagedSlot managedSlot = rpgInventory.getManagedSlots().get(slotId);
            Optional<RpgItemStack> future = itemService.getRpgItemStack(slotTransaction.getFinal().createStack());
            Optional<RpgItemStack> original = itemService.getRpgItemStack(slotTransaction.getOriginal().createStack());

            if (future.isPresent()) {
                RpgItemStack rpgItemStackF = future.get();
                //change
                if (original.isPresent()) {

                    RpgItemStack rpgItemStackO = original.get();

                    boolean k = inventoryHandler.handleCharacterEquipActionPre(character, managedSlot, rpgItemStackF)
                            && inventoryHandler.handleCharacterUnEquipActionPre(character, managedSlot, rpgItemStackO);
                    if (k) {
                        inventoryHandler.handleCharacterUnEquipActionPost(character, managedSlot);
                        inventoryHandler.handleCharacterEquipActionPost(character, managedSlot, rpgItemStackF);
                        character.setRequiresDamageRecalculation(true);
                    } else {
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    //equip
                    //todo do we want to apply custom enchantments from armor from hotbar? I think not => make this configurable
                    ItemClass itemClass = rpgItemStackF.getItemType().getItemClass();
                    if (slotId >= 0 && slotId <= 8 && (itemClass == ItemClass.ARMOR || itemClass == ItemClass.SHIELD)) {
                        continue;
                    }

                    if (inventoryHandler.handleCharacterEquipActionPre(character, managedSlot, rpgItemStackF)) {
                        inventoryHandler.handleCharacterEquipActionPost(character, managedSlot, rpgItemStackF);
                        character.setRequiresDamageRecalculation(true);
                    } else {
                        event.setCancelled(true);
                        return;
                    }

                }

            } else {
                //unequip slot
                if (original.isPresent()) {
                    RpgItemStack rpgItemStack = original.get();
                    if (inventoryHandler.handleCharacterUnEquipActionPre(character, managedSlot, rpgItemStack)) {
                        inventoryHandler.handleCharacterUnEquipActionPost(character, managedSlot);
                    }
                }
            }
        }
    }

    @Listener
    @IsCancelled(Tristate.FALSE)
    public void onDimensionTravel(MoveEntityEvent.Teleport.Portal event, @Root Player player) {
        ISpongeCharacter character = characterService.getCharacter(player);
        if (!character.isStub()) {
            characterService.respawnCharacter(character);
        }
    }


    @Listener(order = Order.LAST)
    @IsCancelled(Tristate.FALSE)
    public void onSwapHands(ChangeInventoryEvent.SwapHand event, @Root Player player) {
        ItemStack futureMainHand = player.getItemInHand(HandTypes.MAIN_HAND).get();
        ItemStack futureOffHand = player.getItemInHand(HandTypes.OFF_HAND).get();
        if (futureMainHand.getType() == ItemTypes.AIR && futureOffHand.getType() == ItemTypes.AIR) {
            return;
        }

        IActiveCharacter character = characterService.getCharacter(player);
        if (character.isStub()) {
            return;
        }

        Optional<RpgItemStack> rpgItemStackOff = itemService.getRpgItemStack(futureOffHand);
        Optional<RpgItemStack> rpgItemStackMain = itemService.getRpgItemStack(futureMainHand);
        if (!rpgItemStackMain.isPresent() && !rpgItemStackOff.isPresent()) {
            return;
        } else {
            Map<Class<?>, RpgInventory> managedInventory = character.getManagedInventory();
            RpgInventory rpgInventory = managedInventory.get(player.getInventory().getClass());

            Hotbar hotbar = player.getInventory()
                    .query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class));
            int selectedSlotIndex = hotbar.getSelectedSlotIndex();

            ManagedSlot managedSlotM = rpgInventory.getManagedSlots().get(selectedSlotIndex);
            ManagedSlot offHandSlotO = rpgInventory.getManagedSlots().get(OFFHAND_SLOT_ID);
            Optional<RpgItemStack> contentM = managedSlotM.getContent();
            Optional<RpgItemStack> contentO = offHandSlotO.getContent();

            if (rpgItemStackOff.isPresent() && !rpgItemStackMain.isPresent()) {
                RpgItemStack futureOff = rpgItemStackOff.get();
                if (inventoryHandler.handleCharacterEquipActionPre(character, offHandSlotO, futureOff)) {
                    managedSlotM.setContent(null);
                    offHandSlotO.setContent(futureOff);
                } else {
                    event.setCancelled(true);
                }
            } else if (!rpgItemStackOff.isPresent()) {
                RpgItemStack futureMain = rpgItemStackMain.get();
                if (inventoryHandler.handleCharacterEquipActionPre(character, managedSlotM, futureMain)) {
                    managedSlotM.setContent(futureMain);
                    offHandSlotO.setContent(null);
                } else {
                    event.setCancelled(true);
                }
            } else {

                RpgItemStack futureOff = rpgItemStackOff.get();
                RpgItemStack futureMain = rpgItemStackMain.get();

                if (inventoryHandler.handleCharacterEquipActionPre(character, offHandSlotO, futureOff) &&
                        inventoryHandler.handleCharacterEquipActionPre(character, managedSlotM, futureMain)
                ) {
                    contentM.ifPresent(offHandSlotO::setContent);
                    contentO.ifPresent(managedSlotM::setContent);
                } else {
                    event.setCancelled(true);
                }
            }
        }

        character.setRequiresDamageRecalculation(true);
    }

    @Listener(order = Order.LAST)
    public void onCharacterHeldItemChange(ChangeInventoryEvent.Held event, @Root Player player) {
        if (player.getOpenInventory().isPresent()) {
            return;
        }

        Hotbar hotbar = player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class));
        int selectedSlotIndex = hotbar.getSelectedSlotIndex();

        Optional<ItemStack> itemInHand = player.getItemInHand(HandTypes.MAIN_HAND);
        if (itemInHand.isPresent()) {
            ItemStack itemStack = itemInHand.get();
            Optional<String> skillBindData = itemStack.get(NKeys.SKILLBIND);
            if (skillBindData.isPresent()) {
                String skillName = skillBindData.get();
                ISpongeCharacter character = characterService.getCharacter(player);
                if (!character.hasCooldown(skillName)) {
                    PlayerSkillContext skill = character.getSkill(skillName);
                    if (skill != null) {
                        skillService.executeSkill(character, skill);

                        hotbar.setSelectedSlotIndex(event.getOriginalSlot().transform().getInventoryProperty(SlotIndex.class).get().getValue());
                    }
                }
            }
        }
    }
}
