/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.resource;

import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * A <b>SecurityResourceManager</b> is 
 * 
 * @author Andreas Ch. Kapp
 *
 */
public class OLATResourceManager {
	
	private static final Logger log = Tracing.createLoggerFor(OLATResourceManager.class);
	
	private static OLATResourceManager INSTANCE;

	private final DB dbInstance;
	
	/**
	 * @return Singleton
	 */
	public static OLATResourceManager getInstance() {
		return INSTANCE;
	}

	@Autowired
	private OLATResourceManager(DB dbInstance) {
		this.dbInstance = dbInstance;
		INSTANCE = this;
	}
	
	/**
	 * Creates a new OLATResource instance (but does not persist the instance)
	 * @param resource
	 * @return OLATResource
	 */
	public OLATResource createOLATResourceInstance(OLATResourceable resource) {
		return new OLATResourceImpl(resource);
	}
	
	public OLATResource createAndPersistOLATResourceInstance(OLATResourceable resource) {
		OLATResource r = new OLATResourceImpl(resource);
		saveOLATResource(r);
		return r;
	}
	
	/**
	 * Creates a new OLATResource instance (but does not persist the instance)
	 * @param typeName
	 * @return OLATResource
	 */
	public OLATResource createOLATResourceInstance(String typeName) {
		Long id = Long.valueOf(CodeHelper.getForeverUniqueID());
		return new OLATResourceImpl(id, typeName);
	}
	
	/**
	 * Creates a new OLATResource instance (but does not persist the instance)
	 * @param aClass
	 * @return OLATResource
	 */
	public OLATResource createOLATResourceInstance(Class<?> aClass) {
		String typeName = OresHelper.calculateTypeName(aClass);
		return createOLATResourceInstance(typeName);
	}
	
	
	/**
	 * Saves a resource.
	 * @param resource
	 * @return True upon success.
	 */
	public void saveOLATResource(OLATResource resource) {
		if (resource.getResourceableTypeName().length() > 50) throw new AssertException("OlatResource: type length may not exceed 50 chars");
		dbInstance.saveObject(resource);
	}
	
	/**
	 * Delete an existing resource.
	 * @param resource
	 * @return True upon success.
	 */
	public void deleteOLATResource(OLATResource resource) {
		dbInstance.deleteObject(resource);
	}
	
	/**
	 * 
	 * @param resourceable
	 * @return true if resourceable was found and deleted, false if it was
	 * not found.
	 */
	public void deleteOLATResourceable(OLATResourceable resourceable) {
		OLATResource ores = findResourceable(resourceable);
		if (ores == null) return;
		deleteOLATResource(ores);
	}
	
	/**
	 * Find the OLATResource for the resourceable. If not found, a new 
	 * OLATResource is created and returned.
	 * 
	 * @param resourceable
	 * @return an OLATResource representing the resourceable. 
	 */
	public OLATResource findOrPersistResourceable(final OLATResourceable resourceable) {
		if (resourceable.getResourceableTypeName() == null) throw new AssertException("typename of olatresourceable can not be null");
		// First try to find resourceable without synchronization
		OLATResource ores = findResourceable(resourceable);
		if (ores != null) {
			return ores;
		}
		// Second there exists no resourcable => try to find and create(if no exists) in a synchronized block
		//o_clusterOK by:cg
		ores = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(resourceable, () -> {
			log.debug("start synchronized-block in findOrPersistResourceable");
			OLATResource oresSync  = findResourceable(resourceable);
			// if not found, persist it.
			if (oresSync == null ) {
				if(CourseModule.ORES_TYPE_COURSE.equals(resourceable.getResourceableTypeName())) {
				  log.info("OLATResourceManager - createOLATResourceInstance if not found: " + resourceable.getResourceableTypeName() + " " + resourceable.getResourceableId());
				}
				oresSync = createOLATResourceInstance(resourceable);
				saveOLATResource(oresSync);
			}
			return oresSync;
		});
		return ores;
	}
	
	/**
	 * Find a resourceanle
	 * @param resourceable
	 * @return OLATResource object or null if not found.
	 */
	public OLATResource findResourceable(OLATResourceable resourceable) {
		String type = resourceable.getResourceableTypeName(); 
		if (type == null) throw new AssertException("typename of olatresourceable must not be null");
		Long id = resourceable.getResourceableId();
		
		return doQueryResourceable(id, type);
	}
	
	/**
	 * Find a resourceable
	 * @param resourceableId
	 * @return OLATResource object or null if not found.
	 */
	public OLATResource findResourceable(Long resourceableId, String resourceableTypeName){
		return doQueryResourceable(resourceableId, resourceableTypeName);
	}
	
	private OLATResource doQueryResourceable(Long resourceableId, String type){
		if (resourceableId == null) resourceableId = OLATResourceImpl.NULLVALUE;

		String s = "select ori from org.olat.resource.OLATResourceImpl ori where ori.resName = :resname and ori.resId = :resid";

		List<OLATResource> resources = dbInstance.getCurrentEntityManager()
				.createQuery(s, OLATResource.class)
				.setParameter("resname", type)
				.setParameter("resid", resourceableId)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();

		// if not found, it is an empty list
		if (resources.isEmpty()) {
			return null;
		}
		return resources.get(0);
	}
	
	public OLATResource findResourceById(Long key) {
		if (key == null) return null;
		return dbInstance.getCurrentEntityManager().find(OLATResourceImpl.class, key);
	}
	
	public List<OLATResource> findResourceByTypes(List<String> types) {
		if(types == null || types.isEmpty()) return Collections.<OLATResource>emptyList();
		
		String s = "select ori from org.olat.resource.OLATResourceImpl ori where ori.resName in (:restrictedType)";
		return dbInstance.getCurrentEntityManager()
				.createQuery(s, OLATResource.class)
				.setParameter("restrictedType", types)
				.getResultList();
	}
}