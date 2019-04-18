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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Transaction;
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
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.util.Tristate;

import javax.inject.Inject;
import java.util.List;
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

		Inventory query = player.getInventory().query(Hotbar.class);


		inventoryHandler.handleCharacterUnEquipActionPost(character, null);
	}

	@Listener
	public void onHotbarInteract(HandInteractEvent event, @First(typeFilter = Player.class) Player player) {
		IActiveCharacter character = characterService.getCharacter(player.getUniqueId());
		Hotbar query = player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class));
		int selectedSlotIndex = query.getSelectedSlotIndex();
		int last = character.getLastHotbarSlotInteraction();
		if (selectedSlotIndex != last) {
			if (inventoryHandler.handleInventoryInitializationPre(character)) {
				inventoryHandler.handleInventoryInitializationPost(character);
			}
			character.setLastHotbarSlotInteraction(last);
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
		Class<? extends Container> aClass = event.getTargetInventory().getClass();
		final List<SlotTransaction> transactions = event.getTransactions();
		switch (transactions.size()) {
			case 1:
				SlotTransaction slotTransaction = transactions.get(0);
				Slot slot = slotTransaction.getSlot();
				Slot transformed = slot.transform();
				int slotId = transformed.getInventoryProperty(SlotIndex.class).get().getValue();
				if (!inventoryService.isManagedInventory(aClass, slotId)) {
					return;
				}
				IActiveCharacter character = characterService.getCharacter(player);
				RpgInventory rpgInventory = character.getManagedInventory().get(aClass);
				ManagedSlot managedSlot = rpgInventory.getManagedSlots().get(slotId);
				Optional<RpgItemStack> opt = itemService.getRpgItemStack(slotTransaction.getFinal().createStack());
				if (opt.isPresent()) {
					RpgItemStack rpgItemStack = opt.get();
					if (inventoryHandler.handleCharacterEquipActionPre(character, managedSlot, rpgItemStack)) {
						if (managedSlot.getContent().isPresent()) {
							inventoryHandler.handleCharacterUnEquipActionPost(character, managedSlot);
						}
						inventoryHandler.handleCharacterEquipActionPost(character, managedSlot, rpgItemStack);
						character.requiresDamageRecalculation();
					}
				} else {
					event.setCancelled(true);
					return;
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

	@Listener
	public void onItemDestruct(ChangeEntityEquipmentEvent.TargetPlayer event) {
		Optional<Transaction<ItemStackSnapshot>> itemStack = event.getItemStack();
		if (itemStack.isPresent()) {
			Transaction<ItemStackSnapshot> transaction = itemStack.get();
			ItemStackSnapshot aFinal = transaction.getFinal();
			if (aFinal.getType() == ItemTypes.AIR) {
				inventoryHandler.handleCharacterUnEquipActionPost(null, null);
			}
		}

	}

}
