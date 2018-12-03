package io.github.winx64.configuration;

import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * An implementation to handle configurations using the .json format
 * 
 * @author WinX64
 *
 */
public class JsonConfiguration extends FileConfiguration {

	private static final Gson GSON;

	static {
		GSON = new GsonBuilder()
				/* Serializers */
				.registerTypeHierarchyAdapter(Map.class,
						(JsonSerializer<Map<String, Object>>) JsonConfiguration::serializeMap)
				.registerTypeHierarchyAdapter(List.class,
						(JsonSerializer<List<Object>>) JsonConfiguration::serializeArray)
				.registerTypeHierarchyAdapter(ConfigurationSerializable.class,
						(JsonSerializer<ConfigurationSerializable>) JsonConfiguration::serializeConfiguration)
				/* Deserializers */
				.registerTypeHierarchyAdapter(Map.class,
						(JsonDeserializer<Map<String, Object>>) JsonConfiguration::deserializeMap)
				.registerTypeHierarchyAdapter(List.class,
						(JsonDeserializer<List<Object>>) JsonConfiguration::deserializeArray)
				.registerTypeHierarchyAdapter(ConfigurationSerializable.class,
						(JsonDeserializer<ConfigurationSerializable>) JsonConfiguration::deserializeConfiguration)
				.setPrettyPrinting().serializeNulls().create();
	}

	@Override
	protected String buildHeader() {
		return "";
	}

	@Override
	public void loadFromString(String contents) throws InvalidConfigurationException {
		Map<?, ?> map;
		try {
			map = GSON.fromJson(contents, Map.class);
		} catch (Exception e) {
			throw new InvalidConfigurationException("Failed to parse the content", e);
		}

		for (Entry<?, ?> entry : map.entrySet()) {
			String key = entry.getKey().toString();
			Object value = entry.getValue();

			if (value instanceof Map) {
				this.createSection(key, (Map<?, ?>) value);
			} else {
				this.set(key, value);
			}
		}
	}

	@Override
	public String saveToString() {
		return GSON.toJson(this.map);
	}

	public static JsonConfiguration loadConfiguration(File file) {
		Validate.notNull(file, "File cannot be null");
		JsonConfiguration config = new JsonConfiguration();

		try {
			config.load(file);
		} catch (IOException | InvalidConfigurationException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, e);
		}

		return config;
	}

	private static JsonElement serializeMap(Map<String, Object> src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject object = new JsonObject();

		for (Entry<String, Object> entry : src.entrySet()) {
			object.add(entry.getKey(), serializeValue(entry.getValue(), context));
		}

		System.out.println(object);

		return object;
	}

	private static JsonElement serializeArray(List<Object> src, Type srcOfType, JsonSerializationContext context) {
		JsonArray array = new JsonArray();

		for (Object object : src) {
			array.add(serializeValue(object, context));
		}

		return array;
	}

	private static JsonElement serializeConfiguration(ConfigurationSerializable src, Type typeOfSrc,
			JsonSerializationContext context) {
		Map<String, Object> map = new LinkedHashMap<>();
		String typeToken = ConfigurationSerialization.getAlias(src.getClass());

		map.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, typeToken);
		map.putAll(src.serialize());

		return context.serialize(map, Map.class);
	}

	private static Map<String, Object> deserializeMap(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) {
		if (!json.isJsonObject()) {
			throw new JsonParseException("Expected JsonObject, got " + json.getClass().getSimpleName());
		}

		JsonObject object = json.getAsJsonObject();
		Map<String, Object> map = new LinkedHashMap<>(object.size(), 1.0F);
		for (Entry<String, JsonElement> entry : object.entrySet()) {
			map.put(entry.getKey(), deserializeValue(entry.getValue(), context));
		}
		return map;
	}

	private static List<Object> deserializeArray(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
		if (!json.isJsonArray()) {
			throw new JsonParseException("Expected JsonArray, got " + json.getClass().getSimpleName());
		}

		JsonArray array = json.getAsJsonArray();
		List<Object> list = new ArrayList<>(array.size());
		for (JsonElement element : array) {
			list.add(deserializeValue(element, context));
		}
		return list;
	}

	private static ConfigurationSerializable deserializeConfiguration(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) {
		if (!json.isJsonObject()) {
			throw new JsonParseException("Expected JsonObject, got " + json.getClass().getSimpleName());
		}

		Map<String, Object> map = context.deserialize(json, Map.class);
		try {
			return ConfigurationSerialization.deserializeObject(map);
		} catch (Exception e) {
			throw new JsonParseException(e);
		}
	}

	private static JsonElement serializeValue(Object object, JsonSerializationContext context) {
		if (object instanceof MemorySection) {
			MemorySection section = (MemorySection) object;
			Map<String, Object> map = section.getKeys(false).stream().collect(toMap(key -> key, section::get));
			return context.serialize(map, Map.class);
		} else if (object instanceof ConfigurationSerializable) {
			return context.serialize(object, ConfigurationSerializable.class);
		} else if (object instanceof List) {
			return context.serialize(object, List.class);
		}
		return context.serialize(object);
	}

	private static Object deserializeValue(JsonElement element, JsonDeserializationContext context) {
		if (element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if (object.has(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
				return context.deserialize(element, ConfigurationSerializable.class);
			} else {
				return context.deserialize(element, Map.class);
			}
		} else if (element.isJsonArray()) {
			return context.deserialize(element, List.class);
		} else if (element.isJsonPrimitive()) {
			JsonPrimitive primitive = element.getAsJsonPrimitive();
			if (primitive.isString()) {
				return primitive.getAsString();
			} else if (primitive.isBoolean()) {
				return primitive.getAsBoolean();
			} else if (primitive.isNumber()) {
				String numberString = primitive.getAsString();
				if (numberString.contains(".")) {
					return primitive.getAsDouble();
				}
				int number = primitive.getAsInt();
				if (numberString.equals(String.valueOf(number))) {
					return primitive.getAsInt();
				}
				return primitive.getAsLong();
			}
			throw new JsonParseException("Unknown json primitive: " + primitive.getClass().getSimpleName());
		} else if (element.isJsonNull()) {
			return null;
		}
		throw new JsonParseException("Unknown json type: " + element.getClass().getSimpleName());
	}
}
