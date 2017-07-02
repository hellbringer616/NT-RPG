package cz.neumimto.rpg.inventory.data;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.immutable.ImmutableListValue;
import org.spongepowered.api.data.value.immutable.ImmutableMapValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.data.value.mutable.MapValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.text.Text;

import java.util.*;
import java.util.function.Supplier;

public class CustomItemData extends AbstractData<CustomItemData, CustomItemData.Immutable> {

	private Integer itemLevel;
	private List<String> restrictions;
	private Map<String, String> enchantements;
	private Text rarity;
	private Integer socketCount;

	public CustomItemData(Integer itemLevel, List<String> restrictions, Map<String, String> enchantements,
	                      Text rarity, Integer socketCount) {
		this.itemLevel = itemLevel;
		this.restrictions = restrictions;
		this.enchantements = enchantements;
		this.rarity = rarity;
		this.socketCount = socketCount;
		registerGettersAndSetters();
	}

	public CustomItemData() {
		restrictions = new ArrayList<>();
		itemLevel = 0;
		enchantements = new HashMap<>();
		rarity = Text.EMPTY;
		socketCount = 0;

		registerGettersAndSetters();
	}

	@Override
	protected void registerGettersAndSetters() {
		registerFieldGetter(NKeys.CUSTOM_ITEM_DATA_ITEM_LEVEL, () -> this.itemLevel);
		registerFieldGetter(NKeys.CUSTOM_ITEM_DATA_RESTRICTIONS, () -> this.restrictions);
		registerFieldGetter(NKeys.CUSTOM_ITEM_DATA_ENCHANTEMENTS, () -> this.enchantements);
		registerFieldGetter(NKeys.ITEM_RARITY, () -> this.rarity);
		registerFieldGetter(NKeys.CUSTOM_ITEM_DATA_SOCKET_COUNT, () -> this.socketCount);

		registerKeyValue(NKeys.CUSTOM_ITEM_DATA_ITEM_LEVEL, this::itemLevel);
		registerKeyValue(NKeys.CUSTOM_ITEM_DATA_RESTRICTIONS, this::groupRestricitons);
		registerKeyValue(NKeys.CUSTOM_ITEM_DATA_ENCHANTEMENTS, this::enchantements);
		registerKeyValue(NKeys.ITEM_RARITY, this::rarity);
		registerKeyValue(NKeys.CUSTOM_ITEM_DATA_SOCKET_COUNT, this::socketCount);
	}

	public Integer getItemLevel() {
		return itemLevel;
	}

	public void setItemLevel(Integer itemLevel) {
		this.itemLevel = itemLevel;
	}

	public List<String> getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(List<String> restrictions) {
		this.restrictions = restrictions;
	}

	public Map<String, String> getEnchantements() {
		return enchantements;
	}

	public void setEnchantements(Map<String, String> enchantements) {
		this.enchantements = enchantements;
	}

	public Text getRarity() {
		return rarity;
	}

	public void setRarity(Text rarity) {
		this.rarity = rarity;
	}

	public Integer getSocketCount() {
		return socketCount;
	}

	public void setSocketCount(Integer socketCount) {
		this.socketCount = socketCount;
	}

	public Value<Integer> itemLevel() {
		return Sponge.getRegistry().getValueFactory().createValue(NKeys.CUSTOM_ITEM_DATA_ITEM_LEVEL, itemLevel);
	}

	public Value<Integer> socketCount() {
		return Sponge.getRegistry().getValueFactory().createValue(NKeys.CUSTOM_ITEM_DATA_SOCKET_COUNT, itemLevel);
	}

	public MapValue<String, String> enchantements() {
		return Sponge.getRegistry().getValueFactory().createMapValue(NKeys.CUSTOM_ITEM_DATA_ENCHANTEMENTS, enchantements);
	}

	public ListValue<String> groupRestricitons() {
		return Sponge.getRegistry().getValueFactory().createListValue(NKeys.CUSTOM_ITEM_DATA_RESTRICTIONS, restrictions);
	}

	public Value<Text> rarity() {
		return Sponge.getRegistry().getValueFactory().createValue(NKeys.ITEM_RARITY, rarity);
	}


	@Override
	public Optional<CustomItemData> fill(DataHolder dataHolder, MergeFunction overlap) {
		Optional<CustomItemData> otherData_ = dataHolder.get(CustomItemData.class);
		if (otherData_.isPresent()) {
			CustomItemData otherData = otherData_.get();
			CustomItemData finalData = overlap.merge(this, otherData);
			this.itemLevel = finalData.itemLevel;
			this.enchantements = finalData.enchantements;
			this.restrictions = finalData.restrictions;
			this.rarity = finalData.rarity;
			this.socketCount = finalData.socketCount;
		}
		return Optional.of(this);
	}

	@Override
	public Optional<CustomItemData> from(DataContainer container) {
		return from((DataView) container);
	}

	public Optional<CustomItemData> from(DataView view) {
		if (view.contains(NKeys.CUSTOM_ITEM_DATA_RESTRICTIONS.getQuery())) {
			this.restrictions = (List<String>) view.getList(NKeys.CUSTOM_ITEM_DATA_RESTRICTIONS.getQuery()).orElseGet(() -> new ArrayList<>());
		}
		if (view.contains(NKeys.CUSTOM_ITEM_DATA_ENCHANTEMENTS.getQuery())) {
			Optional map = view.getMap(NKeys.CUSTOM_ITEM_DATA_ENCHANTEMENTS.getQuery());
			enchantements = (Map<String, String>) map.orElseGet(() -> new HashMap<>());
		}

		if (view.contains(NKeys.CUSTOM_ITEM_DATA_ITEM_LEVEL.getQuery())) {
			this.itemLevel = view.getObject(NKeys.CUSTOM_ITEM_DATA_ITEM_LEVEL.getQuery(), Integer.class).orElseGet(() -> 0);

		}
		if (view.contains(NKeys.ITEM_RARITY)) {
			this.rarity = view.getObject(NKeys.ITEM_RARITY.getQuery(), Text.class).orElseGet(() -> Text.EMPTY);
		}
		if (view.contains(NKeys.CUSTOM_ITEM_DATA_SOCKET_COUNT)) {
			this.socketCount = view.getObject(NKeys.CUSTOM_ITEM_DATA_SOCKET_COUNT.getQuery(), Integer.class).orElseGet(() -> 0);
		}

		return Optional.of(this);

	}

	@Override
	public CustomItemData copy() {
		return new CustomItemData(this.itemLevel, this.restrictions, this.enchantements, this.rarity, this.socketCount);
	}

	@Override
	public Immutable asImmutable() {
		return new Immutable(this.itemLevel, this.restrictions, this.enchantements, this.rarity, this.socketCount);
	}

	@Override
	public int getContentVersion() {
		return 1;
	}

	@Override
	public DataContainer toContainer() {
		DataContainer container =  super.toContainer();
		if (restrictions != null) {
			container = container.set(NKeys.CUSTOM_ITEM_DATA_RESTRICTIONS.getQuery(), this.restrictions);
		}
		if (enchantements != null) {
			container = container.set(NKeys.CUSTOM_ITEM_DATA_ENCHANTEMENTS.getQuery(), this.enchantements);
		}
		if (itemLevel != null) {
			container = container.set(NKeys.CUSTOM_ITEM_DATA_ITEM_LEVEL.getQuery(), this.itemLevel);
		}

		if (rarity != null) {
			container = container.set(NKeys.ITEM_RARITY.getQuery(), this.rarity);
		}

		if (socketCount != null) {
			container = container.set(NKeys.CUSTOM_ITEM_DATA_SOCKET_COUNT.getQuery(), this.socketCount);
		}
		return container;
	}


	public static class Immutable extends AbstractImmutableData<Immutable, CustomItemData> {

		private Integer itemLevel;
		private List<String> restrictions;
		private Map<String, String> enchantements;
		private Text rarity;
		private Integer socketCount;

		public Immutable(Integer itemLevel, List<String> restrictions, Map<String, String> enchantements,
		                 Text rarity, Integer socketCount) {
			this.itemLevel = itemLevel;
			this.restrictions = restrictions;
			this.enchantements = enchantements;
			this.rarity = rarity;
			this.socketCount = socketCount;

			registerGetters();
		}

		@Override
		protected void registerGetters() {
			registerFieldGetter(NKeys.CUSTOM_ITEM_DATA_ITEM_LEVEL, () -> this.itemLevel);
			registerFieldGetter(NKeys.CUSTOM_ITEM_DATA_ENCHANTEMENTS, () -> this.enchantements);
			registerFieldGetter(NKeys.CUSTOM_ITEM_DATA_RESTRICTIONS, () -> this.restrictions);
			registerFieldGetter(NKeys.ITEM_RARITY, () -> this.rarity);
			registerFieldGetter(NKeys.CUSTOM_ITEM_DATA_SOCKET_COUNT, () -> this.socketCount);

			registerKeyValue(NKeys.CUSTOM_ITEM_DATA_ITEM_LEVEL, this::itemLevel);
			registerKeyValue(NKeys.CUSTOM_ITEM_DATA_ENCHANTEMENTS, this::enchantements);
			registerKeyValue(NKeys.CUSTOM_ITEM_DATA_RESTRICTIONS, this::groupRestricitons);
			registerKeyValue(NKeys.ITEM_RARITY, this::rarity);
			registerKeyValue(NKeys.CUSTOM_ITEM_DATA_SOCKET_COUNT, this::socketCount);
		}

		public ImmutableValue<Integer> itemLevel() {
			return Sponge.getRegistry().getValueFactory().createValue(NKeys.CUSTOM_ITEM_DATA_ITEM_LEVEL, itemLevel).asImmutable();
		}

		public ImmutableValue<Integer> socketCount() {
			return Sponge.getRegistry().getValueFactory().createValue(NKeys.CUSTOM_ITEM_DATA_SOCKET_COUNT, itemLevel).asImmutable();
		}

		public ImmutableMapValue<String, String> enchantements() {
			return Sponge.getRegistry().getValueFactory().createMapValue(NKeys.CUSTOM_ITEM_DATA_ENCHANTEMENTS, enchantements).asImmutable();
		}

		public ImmutableListValue<String> groupRestricitons() {
			return Sponge.getRegistry().getValueFactory().createListValue(NKeys.CUSTOM_ITEM_DATA_RESTRICTIONS, restrictions).asImmutable();
		}

		public ImmutableValue<Text> rarity() {
			return Sponge.getRegistry().getValueFactory().createValue(NKeys.ITEM_RARITY, rarity).asImmutable();
		}

		@Override
		public CustomItemData asMutable() {
			return new CustomItemData(itemLevel, restrictions, enchantements, rarity, socketCount);
		}


		@Override
		public DataContainer toContainer() {
			DataContainer container =  super.toContainer();
			if (restrictions != null) {
				container = container.set(NKeys.CUSTOM_ITEM_DATA_RESTRICTIONS.getQuery(), this.restrictions);
			}
			if (enchantements != null) {
				container = container.set(NKeys.CUSTOM_ITEM_DATA_ENCHANTEMENTS.getQuery(), this.enchantements);
			}
			if (itemLevel != null) {
				container = container.set(NKeys.CUSTOM_ITEM_DATA_ITEM_LEVEL.getQuery(), this.itemLevel);
			}

			if (rarity != null) {
				container = container.set(NKeys.ITEM_RARITY.getQuery(), this.rarity);
			}

			if (socketCount != null) {
				container = container.set(NKeys.CUSTOM_ITEM_DATA_SOCKET_COUNT.getQuery(), this.socketCount);
			}
			return container;
		}

		@Override
		public int getContentVersion() {
			return 0;
		}
	}

	public static class Builder extends AbstractDataBuilder<CustomItemData> implements DataManipulatorBuilder<CustomItemData, Immutable> {
		public Builder() {
			super(CustomItemData.class, 1);
		}

		@Override
		public CustomItemData create() {
			return new CustomItemData();
		}

		@Override
		public Optional<CustomItemData> createFrom(DataHolder dataHolder) {
			return create().fill(dataHolder);
		}

		@Override
		protected Optional<CustomItemData> buildContent(DataView container) throws InvalidDataException {
			return create().from(container);
		}
	}
}