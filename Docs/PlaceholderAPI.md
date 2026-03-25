# Documentación de Placeholders - EconomySystem

Esta documentación describe los placeholders disponibles en el plugin EconomySystem a través de la integración con PlaceholderAPI.

## Requisitos

- PlaceholderAPI instalado en el servidor

## Placeholders Disponibles

### Balance del Jugador Actual

Placeholders para mostrar el balance del jugador que está viendo la información:

| Placeholder | Descripción | Ejemplo de Salida |
|------------|-------------|-------------------|
| `%economysystem_balance%` | Balance según configuración de `decimal-places` | `1,500` o `1,500.00` |
| `%economysystem_balance_raw%` | Balance sin formato (para cálculos) | `1500.0` |
| `%economysystem_balance_int%` | Balance como entero | `1500` |
| `%economysystem_balance_decimals%` | Balance con 2 decimales | `1,500.00` |
| `%economysystem_balance_symbol%` | Balance con símbolo de moneda | `$ 1,500` |

#### Ejemplos:

- `%economysystem_balance%` → `1,500` (con `decimal-places: 0`)
- `%economysystem_balance%` → `1,500.00` (con `decimal-places: 2`)
- `%economysystem_balance_int%` → `1500` (siempre entero)
- `%economysystem_balance_decimals%` → `1,500.00` (siempre 2 decimales)
- `%economysystem_balance_symbol%` → `$ 1,500`

---

### Balance de Otro Jugador

Placeholders para mostrar el balance de un jugador específico por nombre:

| Placeholder | Descripción | Ejemplo |
|------------|-------------|---------|
| `%economysystem_balance_other_<nombre>%` | Balance según configuración | `%economysystem_balance_other_Juan%` |
| `%economysystem_balance_other_<nombre>_raw%` | Sin formato | `%economysystem_balance_other_Juan_raw%` |
| `%economysystem_balance_other_<nombre>_int%` | Como entero | `%economysystem_balance_other_Juan_int%` |
| `%economysystem_balance_other_<nombre>_decimals%` | Con 2 decimales | `%economysystem_balance_other_Juan_decimals%` |
| `%economysystem_balance_other_<nombre>_symbol%` | Con símbolo de moneda | `%economysystem_balance_other_Juan_symbol%` |

#### Ejemplos:

- `%economysystem_balance_other_Juan%` → `5,000`
- `%economysystem_balance_other_Juan_int%` → `5000`
- `%economysystem_balance_other_Juan_decimals%` → `5,000.00`
- `%economysystem_balance_other_Juan_symbol%` → `$ 5,000`

---

### Leaderboard (Clasificación de Dinero)

Placeholders para mostrar información sobre los jugadores con más dinero en el servidor:

| Placeholder | Descripción | Ejemplo |
|------------|-------------|---------|
| `%economysystem_leaderboard_<pos>_name%` | Nombre del jugador en posición X | `%economysystem_leaderboard_1_name%` |
| `%economysystem_leaderboard_<pos>_value%` | Balance según configuración | `%economysystem_leaderboard_1_value%` |
| `%economysystem_leaderboard_<pos>_value_raw%` | Balance sin formato | `%economysystem_leaderboard_1_value_raw%` |
| `%economysystem_leaderboard_<pos>_value_int%` | Balance como entero | `%economysystem_leaderboard_1_value_int%` |
| `%economysystem_leaderboard_<pos>_value_decimals%` | Balance con 2 decimales | `%economysystem_leaderboard_1_value_decimals%` |
| `%economysystem_leaderboard_<pos>_value_symbol%` | Balance con símbolo de moneda | `%economysystem_leaderboard_1_value_symbol%` |

#### Ejemplos:

- `%economysystem_leaderboard_1_name%` → `JugadorTop1`
- `%economysystem_leaderboard_1_value%` → `100,000`
- `%economysystem_leaderboard_1_value_int%` → `100000`
- `%economysystem_leaderboard_1_value_symbol%` → `$ 100,000`
- `%economysystem_leaderboard_5_name%` → `JugadorTop5`
- `%economysystem_leaderboard_5_value_decimals%` → `50,000.00`

---

### Configuración del Sistema

Placeholders para mostrar información de configuración del plugin:

| Placeholder | Descripción | Ejemplo |
|------------|-------------|---------|
| `%economysystem_currency_symbol%` | Símbolo de la moneda | `$` |
| `%economysystem_currency_name%` | Nombre de la moneda | `Dollar` |

---

## Configuración del Formato

En `config.yml` puedes configurar cómo se muestran los números:

```yaml
economy:
    currency-symbol: "$"
    currency-name: "Dollar"
    start-balance: 100.0
    # Formato de visualización del dinero
    # 0 = solo enteros (1,500)
    # 2 = dos decimales (1,500.00)
    decimal-places: 0
```

| `decimal-places` | Formato de Salida | Ejemplo |
|-----------------|-------------------|---------|
| `0` | Solo enteros con comas | `1,500` |
| `1` | Un decimal | `1,500.0` |
| `2` | Dos decimales | `1,500.00` |

**Nota:** Los placeholders `_int`, `_decimals`, `_raw` y `_symbol` siempre muestran su formato específico sin importar la configuración de `decimal-places`.

---

## Uso en Otros Plugins

Estos placeholders pueden ser utilizados en cualquier plugin que admita PlaceholderAPI, como:

- **Chat** (mostrar el saldo del jugador en su mensaje)
- **Tab** (mostrar información en la lista de jugadores)
- **Scoreboard** (mostrar balance en el marcador)
- **HolographicDisplays** (mostrar top de jugadores)
- **SignShop** (mostrar precios dinámicos)
- **Kits** (condiciones basadas en el dinero)
- Y muchos otros plugins que soporten placeholders

---

## Ejemplos de Uso Comunes

### Mostrar balance en el chat
```
&e[%economysystem_balance_symbol%] &f%player_name%: &7%message%
```

### Tablist con balance
```
&e%player_name% &7- &a$%economysystem_balance_int%
```

### Leaderboard en holograma
```
&6&lTop Jugadores
&71. &e%economysystem_leaderboard_1_name% &7- &a%economysystem_leaderboard_1_value_symbol%
&72. &e%economysystem_leaderboard_2_name% &7- &a%economysystem_leaderboard_2_value_symbol%
&73. &e%economysystem_leaderboard_3_name% &7- &a%economysystem_leaderboard_3_value_symbol%
```

---

## Notas Importantes

- El sistema muestra hasta los **10 primeros jugadores** en el leaderboard
- Las posiciones válidas son del **1 al 10**
- Si una posición no existe (por ejemplo, si solo hay 5 jugadores pero se consulta la posición 6), el placeholder devolverá vacío
- Los placeholders se **actualizan automáticamente** cuando cambian los datos
- El balance de otro jugador mostrará `"Jugador no encontrado"` si el jugador nunca ha entrado al servidor
- Se crea automáticamente una cuenta de economía cuando un jugador entra al servidor por primera vez
