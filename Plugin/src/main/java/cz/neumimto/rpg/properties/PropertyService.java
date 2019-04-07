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

package cz.neumimto.rpg.properties;

import cz.neumimto.config.blackjack.and.hookers.NotSoStupidObjectMapper;
import cz.neumimto.rpg.Console;
import cz.neumimto.rpg.NtRpgPlugin;
import cz.neumimto.rpg.api.logging.Log;
import cz.neumimto.rpg.players.attributes.Attribute;
import cz.neumimto.rpg.players.attributes.Attributes;
import cz.neumimto.rpg.utils.Utils;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.Collator;
import java.util.*;
import java.util.function.Supplier;

import static cz.neumimto.rpg.NtRpgPlugin.pluginConfig;
import static cz.neumimto.rpg.api.logging.Log.info;

/**
 * Created by NeumimTo on 28.12.2014.
 */

@Singleton
public class PropertyService {

	@Inject
	private NtRpgPlugin plugin;

	public static final double WALKING_SPEED = 0.1d;
	public static int LAST_ID = 0;
	public static final Supplier<Integer> getAndIncrement = () -> {
		int t = new Integer(LAST_ID);
		LAST_ID++;
		return t;
	};

	private Map<String, Integer> idMap = new HashMap<>();
	private Map<Integer, String> nameMap = new HashMap<>();

	private Map<Integer, Float> defaults = new HashMap<>();

	private Set<Integer> damageRecalc = new HashSet<>();

	private float[] maxValues;

	public void registerProperty(String name, int id) {
		info("A new property " + name + "; assigned id: " + id, pluginConfig.DEBUG);
		idMap.put(name, id);
		nameMap.put(id, name);
	}

	public int getIdByName(String name) {
		return idMap.get(name);
	}

	public boolean exists(String property) {
		return idMap.containsKey(property);
	}

	public String getNameById(Integer id) {
		return nameMap.get(id);
	}

	public void registerDefaultValue(int id, float def) {
		defaults.put(id, def);
	}

	public float getDefaultValue(int id) {
		return defaults.get(id);
	}

	public Map<Integer, Float> getDefaults() {
		return defaults;
	}

	public void init() {
		Path path = Paths.get(NtRpgPlugin.workingDir + File.separator + "properties_dump.info");
		StringBuilder s = new StringBuilder();
		List<String> l = new ArrayList<>(idMap.keySet());
		info(" - found " + l.size() + " Properties", pluginConfig.DEBUG);
		l.sort(Collator.getInstance());
		for (String s1 : l) {
			s.append(s1).append(Utils.LineSeparator);
		}
		try {
			Files.write(path, s.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}

		path = Paths.get(NtRpgPlugin.workingDir + "/Attributes.conf");
		File f = path.toFile();
		if (!f.exists()) {
			Optional<Asset> asset = Sponge.getAssetManager().getAsset(plugin, "Attributes.conf");
			if (!asset.isPresent()) {
				throw new IllegalStateException("Could not find an asset Attributes.conf");
			}
			try {
				asset.get().copyToFile(f.toPath());
			} catch (IOException e) {
				throw new IllegalStateException("Could not create Attributes.conf file", e);
			}
		}
	}

	public void reLoadAttributes() {
		Path path = Paths.get(NtRpgPlugin.workingDir + "/Attributes.conf");
		try {
			ObjectMapper<Attributes> mapper = NotSoStupidObjectMapper.forClass(Attributes.class);
			HoconConfigurationLoader hcl = HoconConfigurationLoader.builder().setPath(path).build();
			Attributes attributes = mapper.bind(new Attributes()).populate(hcl.load());
			attributes.getAttributes().forEach(a -> Sponge.getRegistry().register(Attribute.class, new Attribute(a)));


		} catch (ObjectMappingException | IOException e) {
			e.printStackTrace();
		}
	}

	public void loadMaximalServerPropertyValues() {
		maxValues = new float[LAST_ID];
		for (int i = 0; i < maxValues.length; i++) {
			maxValues[i] = Float.MAX_VALUE;
		}


		Path path = Paths.get(NtRpgPlugin.workingDir, "max_server_property_values.properties");
		File file = path.toFile();
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Set<String> missing = new HashSet<>();
		try (FileInputStream fileInputStream = new FileInputStream(file)) {
			Properties properties = new Properties();
			properties.load(fileInputStream);
			for (String s : idMap.keySet()) {
				Object o = properties.get(s);
				if (o == null) {
					missing.add(s);
					Log.info("Missing property \"" + Console.GREEN + s + Console.RESET + "\" in the file max_server_property_values.properties");
					Log.info(" - Appending the file and setting its default value to 1000; You might want to reconfigure that file.");
					maxValues[getIdByName(s)] = 1000f;
				} else {
					maxValues[getIdByName(s)] = Float.parseFloat(o.toString());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!missing.isEmpty()) {
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
				for (String a : missing) {
					writer.write(a + "=1000" + System.lineSeparator());
				}
			} catch (IOException e) {
				Log.error("Could not append file max_server_property_values.properties", e);
			}
		}
	}

	public void processContainer(Class<?> container) {
		int value;
		for (Field f : container.getDeclaredFields()) {
			if (f.isAnnotationPresent(Property.class)) {
				Property p = f.getAnnotation(Property.class);
				value = PropertyService.getAndIncrement.get();
				try {
					f.setInt(null, value);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					continue;
				}
				if (!p.name().trim().equalsIgnoreCase("")) {
					registerProperty(p.name(), value);
				}
				if (p.default_() != 0f) {
					registerDefaultValue(value, p.default_());
				}
			}
		}
	}

	public float getDefault(Integer key) {
		Float f = defaults.get(key);
		if (f == null) {
			return 0;
		}
		return f;
	}

	public float getMaxPropertyValue(int index) {
		return maxValues[index];
	}

	public Collection<String> getAllProperties() {
		return nameMap.values();
	}


	public void overrideMaxPropertyValue(String s, Float aFloat) {
		if (!nameMap.containsValue(s)) {
			Log.info("Attempt to override default value for a property \""+s+"\". But such property does not exists yet. THe property will be created");
			registerProperty(s, getAndIncrement.get());
		}
		defaults.put(getIdByName(s), aFloat);
		Log.info(" Property \"" + s + "\" default value is now " + aFloat + ". This change wont affect already joined players!");
	}

	public boolean updatingRequiresDamageRecalc(int propertyId) {
		return damageRecalc.contains(propertyId);
	}

	public void addPropertyToRequiresDamageRecalc(int i) {
		damageRecalc.add(i);
	}
}
