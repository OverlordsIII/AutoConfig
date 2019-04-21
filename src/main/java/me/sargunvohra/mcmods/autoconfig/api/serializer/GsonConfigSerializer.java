package me.sargunvohra.mcmods.autoconfig.api.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import me.sargunvohra.mcmods.autoconfig.api.ConfigData;
import me.sargunvohra.mcmods.autoconfig.api.ConfigSerializer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;

public class GsonConfigSerializer<T extends ConfigData> implements ConfigSerializer<T> {

    private String name;
    private Class<T> configClass;
    private Gson gson;

    @SuppressWarnings("WeakerAccess")
    public GsonConfigSerializer(String name, Class<T> configClass, Gson gson) {
        this.name = name;
        this.configClass = configClass;
        this.gson = gson;
    }

    public GsonConfigSerializer(String name, Class<T> configClass) {
        this(name, configClass, new GsonBuilder().setPrettyPrinting().create());
    }

    private Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDirectory().toPath().resolve(name + ".json");
    }

    @Override
    public void serialize(T config) throws SerializationException {
        try {
            BufferedWriter writer = Files.newBufferedWriter(getConfigPath());
            gson.toJson(config, writer);
            writer.close();
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public T deserialize() throws SerializationException {
        Path configPath = getConfigPath();
        if (Files.exists(configPath)) {
            try {
                BufferedReader reader = Files.newBufferedReader(configPath);
                T ret = gson.fromJson(reader, configClass);
                reader.close();
                return ret;
            } catch (IOException | JsonParseException e) {
                throw new SerializationException(e);
            }
        } else {
            return createDefault();
        }
    }

    @Override
    public T createDefault() {
        try {
            Constructor<T> constructor = configClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}