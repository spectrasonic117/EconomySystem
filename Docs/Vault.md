# Documentación de Integración con Vault

Esta documentación explica cómo el plugin EconomySystem se integra con Vault y cómo puedes utilizar esta funcionalidad en tus propios plugins.

## ¿Qué es Vault?

Vault es un plugin de Bukkit/Spigot/Paper que proporciona una capa de abstracción para diferentes sistemas de economía, permisos y chat. Esto permite que los plugins interactúen con estos sistemas sin depender de un plugin específico.

## Integración con EconomySystem

EconomySystem incluye una implementación completa de la interfaz de economía de Vault, lo que significa que cualquier plugin que utilice Vault para realizar operaciones económicas puede trabajar con EconomySystem.

### Funcionalidades Soportadas

- Consulta de saldos: Obtener el dinero de un jugador
- Retiros: Quitar dinero del saldo de un jugador
- Depósitos: Agregar dinero al saldo de un jugador
- Creación de cuentas: Crear nuevas cuentas de jugador
- Verificación de fondos: Comprobar si un jugador tiene suficiente dinero

### No Soportado

- Bancos: EconomySystem no incluye funcionalidad de bancos
- Soporte por mundos: Las operaciones no varían según el mundo

## Cómo Agregar Vault a Tus Plugins de Terceros

Para integrar Vault en tus propios plugins, sigue estos pasos:

### 1. Añadir Vault como dependencia

En tu archivo `plugin.yml`, agrega Vault como dependencia o soft-dependencia:

```yaml
depend: [Vault]
# o
softdepend: [Vault]
```

### 2. Añadir la dependencia Maven/Gradle

Si estás usando Maven, añade esta dependencia a tu `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.MilkBowl</groupId>
        <artifactId>VaultAPI</artifactId>
        <version>1.7</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

Si estás usando Gradle, añade esto a tu `build.gradle`:

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.MilkBowl:VaultAPI:1.7'
}
```

### 3. Implementar la integración en tu código

```java
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class TuPlugin extends JavaPlugin {
    
    private Economy econ = null;
    
    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("No se encontró ningún plugin de economía compatible!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Tu código aquí
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    
    // Métodos de ejemplo para usar la economía
    public boolean hasMoney(String playerName, double amount) {
        return econ.has(playerName, amount);
    }
    
    public void addMoney(String playerName, double amount) {
        econ.depositPlayer(playerName, amount);
    }
    
    public void removeMoney(String playerName, double amount) {
        econ.withdrawPlayer(playerName, amount);
    }
    
    public double getBalance(String playerName) {
        return econ.getBalance(playerName);
    }
}
```

### 4. Manejar la ausencia de Vault

Es recomendable manejar el caso donde Vault no esté disponible:

```java
public class TuPlugin extends JavaPlugin {
    
    private Economy econ = null;
    private boolean vaultEnabled = false;
    
    public boolean hasMoney(String playerName, double amount) {
        if (vaultEnabled && econ != null) {
            return econ.has(playerName, amount);
        } else {
            // Implementación alternativa si Vault no está disponible
            return false;
        }
    }
    
    public void addMoney(String playerName, double amount) {
        if (vaultEnabled && econ != null) {
            econ.depositPlayer(playerName, amount);
        } else {
            // Implementación alternativa si Vault no está disponible
        }
    }
}
```

## Métodos Disponibles de la API de Economía

Una vez que tienes acceso a la instancia de Economy, puedes usar los siguientes métodos:

### Consulta de Información
- `getName()` - Obtiene el nombre del plugin de economía
- `currencyNamePlural()` - Nombre plural de la moneda
- `currencyNameSingular()` - Nombre singular de la moneda
- `format(double amount)` - Formatear una cantidad monetaria
- `fractionalDigits()` - Número de decimales en la moneda

### Operaciones de Cuenta
- `hasAccount(String playerName)` - Verifica si un jugador tiene cuenta
- `createPlayerAccount(String playerName)` - Crea una cuenta para un jugador
- `getBalance(String playerName)` - Obtiene el saldo de un jugador

### Operaciones Económicas
- `has(String playerName, double amount)` - Verifica si un jugador tiene suficiente dinero
- `depositPlayer(String playerName, double amount)` - Deposita dinero en la cuenta de un jugador
- `withdrawPlayer(String playerName, double amount)` - Retira dinero de la cuenta de un jugador

Todos estos métodos están completamente implementados en EconomySystem y disponibles a través de la integración con Vault.

## Consideraciones Específicas de EconomySystem

- El nombre de la moneda se define en la configuración del plugin
- La moneda no tiene decimales (fractionalDigits() devuelve 0)
- No hay soporte para bancos (hasBankSupport() devuelve false)
- No hay diferencias entre mundos en las operaciones económicas