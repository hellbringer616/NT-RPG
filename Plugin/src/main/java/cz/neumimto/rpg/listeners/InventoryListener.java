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

package cz.neumimto.rpg.listeners;

import com.google.inject.Singleton;
import cz.neumimto.rpg.ResourceLoader;
import cz.neumimto.rpg.api.inventory.InventoryService;
import cz.neumimto.rpg.api.inventory.ManagedSlot;
import cz.neumimto.rpg.api.inventory.RpgInventory;
import cz.neumimto.rpg.api.items.RpgItemStack;
import cz.neumimto.rpg.common.inventory.InventoryHandler;
import cz.neumimto.rpg.inventory.SpongeItemService;
import cz.neumimto.rpg.inventory.data.NKeys;
import cz.neumimto.rpg.players.CharacterService;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.utils.ItemStackUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.filter.type.Include;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
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

	@Inject
	private CharacterService characterService;

	@Inject
	private InventoryHandler inventoryHandler;

	@Inject
	private InventoryService inventoryService;

	@Inject
	private SpongeItemService itemService;


	@Listener
	@IsCancelled(Tristate.FALSE)
	public void onItemDrop(DropItemEvent.Dispense event, @Root Player player) {
		if (!player.getOpenInventory().isPresent()) {
			return;
		}
		IActiveCharacter character = characterService.getCharacter(player);

		Hotbar query = player.getInventory().query(Hotbar.class);
        int selectedSlotIndex = query.getSelectedSlotIndex();

        inventoryHandler.handleCharacterUnEquipActionPost(character, null);
	}

	@Listener
	public void onHotbarInteract(HandInteractEvent event, @First(typeFilter = Player.class) Player player) {
		IActiveCharacter character = characterService.getCharacter(player.getUniqueId());
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

					Map<Integer, ManagedSlot> managedSlots = character.getManagedInventory().get(inventory.getClass()).getManagedSlots();

					if (managedSlots.containsKey(selectedSlotIndex)) {
						ManagedSlot managedSlot = managedSlots.get(selectedSlotIndex);
						if (inventoryHandler.handleCharacterEquipActionPre(character, managedSlot, rpgItemType1)) {
							inventoryHandler.handleInventoryInitializationPost(character);
							character.setLastHotbarSlotInteraction(last);
						} else {
							ItemStackUtils.dropItem(player, itemStack);
							player.setItemInHand(HandTypes.MAIN_HAND, ItemStack.empty());
							character.setLastHotbarSlotInteraction(-1);
						}
					}
                }
			}
		}
	}

	@Listener
	@Include({
			ClickInventoryEvent.Primary.class,
			ClickInventoryEvent.Secondary.class
	})
	@IsCancelled(Tristate.FALSE)
	public void onClick(ClickInventoryEvent event, @Root Player player) {
		List<SlotTransaction> transactions = event.getTransactions();
		for (SlotTransaction transaction : transactions) {
			Optional<String> s = transaction.getOriginal().get(NKeys.COMMAND);
			if (s.isPresent()) {
				Sponge.getCommandManager().process(player, s.get());
			}
			Optional<Boolean> aBoolean = transaction.getOriginal().get(NKeys.MENU_INVENTORY);
			if (aBoolean.isPresent()) {
				if (aBoolean.get()) {
					event.setCancelled(true);
				}
			}
		}
	}


	@Listener
	@Include({
			ClickInventoryEvent.Primary.class,
			ClickInventoryEvent.Secondary.class
	})
	public void onInteract(ClickInventoryEvent event, @Root Player player) {
		final List<SlotTransaction> transactions = event.getTransactions();
		switch (transactions.size()) {
			case 1:
				SlotTransaction slotTransaction = transactions.get(0);
				Slot slot = slotTransaction.getSlot();
				Slot transformed = slot.transform();
				Class aClass = transformed.parent().getClass();
				int slotId = transformed.getInventoryProperty(SlotIndex.class).get().getValue();
				if (!inventoryService.isManagedInventory(aClass, slotId)) {
					return;
				}
				IActiveCharacter character = characterService.getCharacter(player);
				RpgInventory rpgInventory = character.getManagedInventory().get(aClass);
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

						} else {
							event.setCancelled(true);
						}
					} else {
						//equip
						if (inventoryHandler.handleCharacterEquipActionPre(character, managedSlot, rpgItemStackF)) {
							inventoryHandler.handleCharacterEquipActionPost(character, managedSlot, rpgItemStackF);
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
				break;
			case 2:
				//???
				break;
			default:
				//???//???
				return;
		}

	}

	@Listener
	@IsCancelled(Tristate.FALSE)
	public void onDimensionTravel(MoveEntityEvent.Teleport.Portal event, @Root Player player) {
		IActiveCharacter character = characterService.getCharacter(player);
		if (!character.isStub()) {
			characterService.respawnCharacter(character);
		}
	}


	@Listener(order = Order.LAST)
	@IsCancelled(Tristate.FALSE)
	public void onSwapHands(ChangeInventoryEvent.SwapHand event, @Root Player player) {
		ItemStack futureMainHand = player.getItemInHand(HandTypes.MAIN_HAND).orElse(null);
		ItemStack futureOffHand = player.getItemInHand(HandTypes.OFF_HAND).orElse(null);

	}

}
