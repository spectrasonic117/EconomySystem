package com.spectrasonic.economySystem.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class MessageManager {

    private final YamlConfiguration config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private String prefix = "";

    public MessageManager(File file) throws IOException {
        // Carga el YAML; si el archivo no existe se lanzará IOException
        this.config = YamlConfiguration.loadConfiguration(file);
        if (config.contains("messages.prefix")) {
            this.prefix = config.getString("messages.prefix");
        }
    }

    public Component get(String key, Object... placeholders) {
        String fullKey = "messages." + key;
        if (!config.contains(fullKey)) {
            return Component.text("Message not found: " + key);
        }
        String message = config.getString(fullKey);
        message = applyPlaceholders(message, placeholders);

        // No añadir prefijo si el mensaje ya lo contiene o es el propio prefijo
        if (key.equals("prefix") || message.startsWith("<green>[EconomySystem]</green>")) {
            return miniMessage.deserialize(message);
        }
        return miniMessage.deserialize(prefix + " " + message);
    }

    private String applyPlaceholders(String message, Object... placeholders) {
        if (placeholders.length % 2 != 0) {
            throw new IllegalArgumentException("Placeholders must be in key-value pairs!");
        }
        for (int i = 0; i < placeholders.length; i += 2) {
            String placeholder = placeholders[i].toString();
            String value = placeholders[i + 1].toString();
            message = message.replace(placeholder, value);
        }
        return message;
    }

    public String getPrefix() {
        return prefix;
    }
}
