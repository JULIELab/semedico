package de.julielab.semedico.core.services.interfaces;

import com.google.common.cache.LoadingCache;

public interface ICacheService {
	public enum Region {TERM, RELATIONSHIP, FACET_ROOTS, ROOT_PATHS, TERM_CHILDREN, ROOT_PATHS_IN_FACET, SHORTEST_ROOT_PATH_IN_FACET};
	
	public <K, E> LoadingCache<K, E> getCache(Region region);
	public <K, E> E get(K key, Region region);
}
