El sistema de caché funciona con un patrón write-through con dirty flag:

**Componentes:**

1. CacheManager (CacheManager.java:12-226)                                                                                                      
    - ConcurrentHashMap<String, CacheEntry> - almacenamiento thread-safe
    - Contadores: hitCount, missCount, flushCount
    - Operaciones:
    - getBalance(): busca en caché, si no existe carga de DB
    - setBalance/addBalance/removeBalance(): modifican en caché y marcan como dirty
    - flushDirtyEntries(): escribe dirty entries a DB de forma asíncrona

2. CacheEntry (CacheEntry.java:3-59)
    - volatile double balance - visibilidad entre threads
    - volatile boolean dirty - flag de modificación pendiente
    - volatile long lastAccess - timestamp de último acceso
    - synchronized en addBalance/removeBalance para atomicidad

3. FlushScheduler (FlushScheduler.java:5-78)
    - Ejecuta flush cada X segundos (configurable)
    - Solo hace flush si dirtyCount ≤ max-batch-size
    - Si excede el límite, espera al siguiente ciclo

  Posibles mejoras:

|           Mejora          	|            Problema actual            	|       Impacto      	|
|:-------------------------:	|:-------------------------------------:	|:------------------:	|
| LRU Eviction              	| Caché crece indefinidamente           	| Memoria            	|
| Batching incremental      	| Espera si dirtyCount > max-batch-size 	| Latencia           	|
| Write-back agresivo       	| Dirty flag puede acumularse           	| Rendimiento        	|
| Persistencia en shutdown  	| No hay save explícito al cerrar       	| Seguridad de datos 	|
| Preloading de top players 	| Carga bajo demanda                    	| Hit rate           	|