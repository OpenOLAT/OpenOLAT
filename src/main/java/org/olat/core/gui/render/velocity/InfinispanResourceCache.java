package org.olat.core.gui.render.velocity;

import java.util.Iterator;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.ResourceCache;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;

/**
 * 
 * Initial date: 26.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InfinispanResourceCache implements ResourceCache {
	
	private CacheWrapper<Object,Resource> cache;

	@Override
	public void initialize(RuntimeServices rs) {
		cache = CoordinatorManager.getInstance().getCoordinator().getCacher().getCache("Velocity", "templates");
	}

	@Override
	public Resource get(Object resourceKey) {
		return cache.get(resourceKey);
	}

	@Override
	public Resource put(Object resourceKey, Resource resource) {
		return cache.put(resourceKey, resource);
	}

	@Override
	public Resource remove(Object resourceKey) {
		return cache.remove(resourceKey);
	}

	@Override
	public Iterator<Object> enumerateKeys() {
		return cache.iterateKeys();
	}
}
