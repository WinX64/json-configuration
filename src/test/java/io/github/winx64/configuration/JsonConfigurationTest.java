package io.github.winx64.configuration;

import java.util.*;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.util.Vector;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class JsonConfigurationTest {

    private final Map<String, Object> testValues;

    public JsonConfigurationTest(Map<String, Object> testValues) {
        this.testValues = testValues;
    }

    @BeforeClass
    public static void configureSerialization() {
        ConfigurationSerialization.registerClass(Vector.class);
    }

    @Test
    public void testConfiguration() throws InvalidConfigurationException {
        FileConfiguration configOne = new JsonConfiguration();
        for (Entry<String, Object> entry : testValues.entrySet()) {
            configOne.set(entry.getKey(), entry.getValue());
        }

        FileConfiguration configTwo = new JsonConfiguration();
        configTwo.loadFromString(configOne.saveToString());

        Assert.assertTrue(equals(configOne, configTwo));
    }

    private boolean equals(ConfigurationSection sectionOne, ConfigurationSection sectionTwo) {
        Set<String> keysOne = sectionOne.getKeys(false);
        Set<String> keysTwo = sectionTwo.getKeys(false);
        if (keysOne.size() != keysTwo.size() || !keysOne.equals(keysTwo)) {
            return false;
        }

        for (String key : keysOne) {
            Object valueOne = sectionOne.get(key);
            Object valueTwo = sectionTwo.get(key);

            if (valueOne == null || valueTwo == null) {
                if (valueOne == valueTwo) {
                    continue;
                } else {
                    return false;
                }
            }

            if (valueOne instanceof ConfigurationSection && valueTwo instanceof ConfigurationSection) {
                if (!equals((ConfigurationSection) valueOne, (ConfigurationSection) valueTwo)) {
                    return false;
                }
                continue;
            }

            if (!valueOne.equals(valueTwo)) {
                return false;
            }
        }

        return true;
    }

    @Parameters
    public static Iterable<? extends Map<String, Object>> getAttributes() {
        Map<String, Object> map = new HashMap<>();

        map.put("String", "myTestString");
        map.put("int", 10);
        map.put("boolean", true);
        map.put("Vector", Vector.getRandom());
        map.put("List", Arrays.asList("myTestString", 10, true));
        map.put("Map.ItemOne", "myTestString");
        map.put("Map.ItemTwo", 10);
        map.put("Map.ItemThree", true);

        return Collections.singletonList(map);
    }
}
