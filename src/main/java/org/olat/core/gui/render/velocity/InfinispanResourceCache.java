/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
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
