package com.spectrasonic.economySystem.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Gestor de mensajes cargado desde un archivo YAML.
 * <p>
 * Los mensajes están bajo la clave raíz "messages" y pueden accederse
 * mediante {@code get("key")}, donde el key corresponde a
 * {@code messages.key}. Se sustituyen los placeholders pasando pares
 * clave‑valor en {@code placeholders}.
 */
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

    /**
     * Obtiene un {@link Component} formateado.
     *
     * @param key          clave del mensaje sin el prefijo "messages."
     * @param placeholders pares clave‑valor para sustitución
     * @return componente listo para enviar
     */
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
