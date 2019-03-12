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

package cz.neumimto.rpg.players.groups;

import cz.neumimto.config.blackjack.and.hookers.annotations.AsCollectionImpl;
import cz.neumimto.config.blackjack.and.hookers.annotations.CustomAdapter;
import cz.neumimto.config.blackjack.and.hookers.annotations.Default;
import cz.neumimto.rpg.configuration.adapters.AllowedArmorListAdapter;
import cz.neumimto.rpg.configuration.adapters.AttributeMapAdapter;
import cz.neumimto.rpg.configuration.adapters.ClassDependencyGraphAdapter;
import cz.neumimto.rpg.configuration.adapters.ClassExpAdapter;
import cz.neumimto.rpg.configuration.adapters.ClassTypeAdapter;
import cz.neumimto.rpg.configuration.adapters.EffectsAdapter;
import cz.neumimto.rpg.configuration.adapters.PropertiesArrayAdapter;
import cz.neumimto.rpg.configuration.adapters.SkillTreeLookupAdapter;
import cz.neumimto.rpg.configuration.adapters.WeaponsAdapter;
import cz.neumimto.rpg.effects.EffectParams;
import cz.neumimto.rpg.effects.EffectSourceType;
import cz.neumimto.rpg.effects.IEffectSource;
import cz.neumimto.rpg.effects.IEffectSourceProvider;
import cz.neumimto.rpg.effects.IGlobalEffect;
import cz.neumimto.rpg.inventory.ConfigRPGItemType;
import cz.neumimto.rpg.inventory.RPGItemType;
import cz.neumimto.rpg.players.ExperienceSource;
import cz.neumimto.rpg.players.attributes.Attribute;
import cz.neumimto.rpg.players.leveling.EmptyLevlProgression;
import cz.neumimto.rpg.players.leveling.ILevelProgression;
import cz.neumimto.rpg.players.leveling.SkillTreeType;
import cz.neumimto.rpg.skills.tree.SkillTree;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by NeumimTo on 27.12.2014.
 */
@ConfigSerializable
public class ClassDefinition implements IEffectSourceProvider {

	@Setting("Name")
	private String name;

	@Setting("Description")
	private String description;

	@Setting("WelcomeMessage")
	private Text welcomeMessage;

	@Setting("PreferredTextColor")
	private TextColor preferedColor;

	@Setting("ItemInfo")
	private ItemStack info;

	@Setting("ItemType")
	private ItemType itemType;

	@Setting("Visible")
	private boolean showsInMenu = true;

	@Setting("ClassType")
	@CustomAdapter(ClassTypeAdapter.class)
	protected String type;

	@Setting("Properties")
	@CustomAdapter(PropertiesArrayAdapter.class)
	private float[] propBonus;

	@Setting("AllowedArmor")
	@CustomAdapter(AllowedArmorListAdapter.class)
	private Set<RPGItemType> allowedArmor = new HashSet<>();

	@Setting("Permissions")
	@AsCollectionImpl(TreeSet.class)
	private Set<PlayerGroupPermission> permissions;

	@Setting("PropertiesLevelBonus")
	@CustomAdapter(PropertiesArrayAdapter.class)
	private float[] propLevelBonus;

	@Setting("ExitCommands")
	@AsCollectionImpl(ArrayList.class)
	private List<String> exitCommands;

	@Setting("EnterCommands")
	@AsCollectionImpl(ArrayList.class)
	private List<String> enterCommands;

	@Setting("ProjectileDamage")
	private Map<EntityType, Double> projectileDamage = new HashMap<>();

	@Setting("Weapons")
	@CustomAdapter(WeaponsAdapter.class)
	private HashMap<ItemType, Set<ConfigRPGItemType>> weapons = new HashMap<>();

	@Setting("Attributes")
	@CustomAdapter(AttributeMapAdapter.class)
	private Map<Attribute, Integer> startingAttributes = new HashMap<>();

	@Setting("Effects")
	@CustomAdapter(EffectsAdapter.class)
	private Map<IGlobalEffect, EffectParams> effects = new HashMap<>();

	@Setting("Offhand")
	@CustomAdapter(WeaponsAdapter.class)
	private HashMap<ItemType, Set<ConfigRPGItemType>> offHandWeapons = new HashMap<>();

	@Setting("Experiences")
	@CustomAdapter(ClassExpAdapter.class)
	private Map<String, Map<EntityType, Double>> experiences = new HashMap<>();

	@Setting("SkillTreeId")
	@CustomAdapter(SkillTreeLookupAdapter.class)
	private SkillTree skillTree;

	@Setting("SkillPointsPerLevel")
	private int skillpointsPerLevel;

	@Setting("AttributePointsPerLevel")
	private int attributepointsPerLevel;

	@Setting("Leveling")
	@Default(EmptyLevlProgression.class)
	private ILevelProgression levels;

	@Setting("SkillTreeType")
	private SkillTreeType skillTreeType;

	@Setting("ExperienceSources")
	@AsCollectionImpl(HashSet.class)
	private Set<ExperienceSource> experienceSourceSet;

	@Setting("Default")
	private boolean defaultClass;

	@Setting("Dependencies")
	@CustomAdapter(ClassDependencyGraphAdapter.class)
	private DependencyGraph classDefinitionDependencyGraph;

	@Setting("CustomLore")
	@AsCollectionImpl(ArrayList.class)
	private List<Text> customLore;

	public ClassDefinition(String name, String classType) {
		this.name = name;
		this.type = classType;
		this.classDefinitionDependencyGraph = new DependencyGraph(this);
	}

	public String getName() {
		return name;
	}

	public ItemStack getInfo() {
		return info;
	}

	public void setInfo(ItemStack info) {
		this.info = info;
	}

	public boolean showsInMenu() {
		return showsInMenu;
	}

	public float[] getPropBonus() {
		return propBonus;
	}

	public boolean isShowsInMenu() {
		return showsInMenu;
	}

	public Set<RPGItemType> getAllowedArmor() {
		return allowedArmor;
	}

	public Map<ItemType, Set<ConfigRPGItemType>> getWeapons() {
		return weapons;
	}

	public HashMap<ItemType, Set<ConfigRPGItemType>> getOffHandWeapons() {
		return offHandWeapons;
	}

	public Set<PlayerGroupPermission> getPermissions() {
		return Collections.unmodifiableSet(permissions);
	}

	public void setPermissions(Set<PlayerGroupPermission> permissions) {
		this.permissions = new TreeSet<>(permissions);
	}

	public float[] getPropLevelBonus() {
		return propLevelBonus;
	}

	public ItemType getItemType() {
		return itemType;
	}

	public void setItemType(ItemType itemType) {
		this.itemType = itemType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Map<Attribute, Integer> getStartingAttributes() {
		return startingAttributes;
	}

	public String getClassType() {
		return type;
	}

	public Map<IGlobalEffect, EffectParams> getEffects() {
		return effects;
	}

	public void setEffects(Map<IGlobalEffect, EffectParams> effects) {
		this.effects = effects;
	}

	public Map<EntityType, Double> getProjectileDamage() {
		return projectileDamage;
	}

	public List<String> getExitCommands() {
		return exitCommands;
	}

	public List<String> getEnterCommands() {
		return enterCommands;
	}

	public TextColor getPreferedColor() {
		return preferedColor;
	}

	public double getExperiencesBonus(String dimmension, EntityType type) {
		Map<EntityType, Double> entityTypeDoubleMap = getExperiences().get(dimmension);
		if (entityTypeDoubleMap == null) {
			return 0;
		}
		Double aDouble = entityTypeDoubleMap.get(type);
		return aDouble == null ? 0 : aDouble;
	}

	public Map<String, Map<EntityType, Double>> getExperiences() {
		return experiences;
	}

	public DependencyGraph getClassDependencyGraph() {
		return classDefinitionDependencyGraph;
	}

	public boolean hasExperienceSource(ExperienceSource source) {
		return experienceSourceSet.contains(source);
	}

	public List<Text> getCustomLore() {
		return customLore;
	}

	public SkillTree getSkillTree() {
		return skillTree;
	}

	public void setSkillTree(SkillTree skillTree) {
		this.skillTree = skillTree;
	}

	public int getSkillpointsPerLevel() {
		return skillpointsPerLevel;
	}

	public int getAttributepointsPerLevel() {
		return attributepointsPerLevel;
	}

	@Override
	public IEffectSource getType() {
		return EffectSourceType.CLASS;
	}

	public ILevelProgression getLevelProgression() {
		return levels;
	}

	public Text getWelcomeMessage() {
		return welcomeMessage;
	}

	public SkillTreeType getSkillTreeType() {
		return skillTreeType;
	}

	@Override
	public String toString() {
		return "ClassDefinition{" +
				"name='" + name + '\'' +
				", type=" + type +
				'}';
	}
}
