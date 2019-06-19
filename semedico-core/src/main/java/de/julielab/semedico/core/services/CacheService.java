package de.julielab.semedico.core.services;

import com.google.common.cache.Cache;
import com.google.common.cache.LoadingCache;
import de.julielab.semedico.core.services.interfaces.ICacheService;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CacheService implements ICacheService {

	Map<Region, Cache<?, ?>> caches;
	private Logger log;

	public CacheService(Logger log, Map<Region, CacheWrapper> configuration) {
		this.log = log;
		caches = new HashMap<>();
		for (Entry<Region, CacheWrapper> entry : configuration.entrySet()) {
			caches.put(entry.getKey(), entry.getValue().cache);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K, E> LoadingCache<K, E> getCache(Region region) {
		Cache<?, ?> cache = caches.get(region);
		return (LoadingCache<K, E>) cache;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K, E> E get(K key, Region region) {
		E cache = (E) caches.get(region).getIfPresent(key);
		if (cache == null)
			log.warn("No cache for region \"" + region.name() + "\" was found.");
		return cache;
	}

	public static class CacheWrapper {
		Cache<?, ?> cache;

		public CacheWrapper(Cache<?, ?> cache) {
			this.cache = cache;
		}

	}
}
