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

package org.olat.admin.quota;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <h3>Description:</h3>
 * Quota manager implementation for the OLAT LMS. This is a singleton that must
 * be specified in the spring configuration and be properly initialized!
 * <p>
 * Initial Date: 23.05.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
@Service("org.olat.core.util.vfs.QuotaManager")
public class QuotaManagerImpl implements QuotaManager, InitializingBean {
	private static final OLog log = Tracing.createLoggerFor(QuotaManagerImpl.class);

	private static final String QUOTA_CATEGORY = "quot";
	private OLATResource quotaResource;
	private final Map<String,Quota> defaultQuotas = new ConcurrentHashMap<>();
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PropertyManager propertyManager;
	@Autowired
	private OLATResourceManager resourceManager;
	

	/**
	 * @see org.olat.core.util.vfs.QuotaManager#createQuota(java.lang.String, java.lang.Long, java.lang.Long)
	 */
	@Override
	public Quota createQuota(String path, Long quotaKB, Long ulLimitKB) {
		return new QuotaImpl(path, quotaKB, ulLimitKB);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		quotaResource = resourceManager.findOrPersistResourceable(OresHelper.lookupType(Quota.class));
		initDefaultQuotas(); // initialize default quotas
		dbInstance.intermediateCommit();
		log.info("Successfully initialized Quota Manager");
	}

	private void initDefaultQuotas() {
		Quota defaultQuotaUsers = initDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_USERS);
		defaultQuotas.put(QuotaConstants.IDENTIFIER_DEFAULT_USERS, defaultQuotaUsers);
		Quota defaultQuotaPowerusers = initDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_POWER);
		defaultQuotas.put(QuotaConstants.IDENTIFIER_DEFAULT_POWER, defaultQuotaPowerusers);
		Quota defaultQuotaGroups = initDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_GROUPS);
		defaultQuotas.put(QuotaConstants.IDENTIFIER_DEFAULT_GROUPS, defaultQuotaGroups);
		Quota defaultQuotaRepository = initDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_REPO);
		defaultQuotas.put(QuotaConstants.IDENTIFIER_DEFAULT_REPO, defaultQuotaRepository);
		Quota defaultQuotaCourseFolder = initDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_COURSE);
		defaultQuotas.put(QuotaConstants.IDENTIFIER_DEFAULT_COURSE, defaultQuotaCourseFolder);
		Quota defaultQuotaNodeFolder = initDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_NODES);
		defaultQuotas.put(QuotaConstants.IDENTIFIER_DEFAULT_NODES, defaultQuotaNodeFolder);
		Quota defaultQuotaFeed = initDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_FEEDS);
		defaultQuotas.put(QuotaConstants.IDENTIFIER_DEFAULT_FEEDS, defaultQuotaFeed);
	}

	/**
	 * 
	 * @param quotaIdentifier
	 * @param factor Multiplier for some long running resources as blogs
	 * @return
	 */
	private Quota initDefaultQuota(String quotaIdentifier) {
		Quota q = null;
		Property p = propertyManager.findProperty(null, null, quotaResource, QUOTA_CATEGORY, quotaIdentifier);
		if (p != null) {
			q = parseQuota(p);
		}
		if (q != null) {
			return q;
		}
		// initialize default quota
		q = createQuota(quotaIdentifier, Long.valueOf(FolderConfig.getDefaultQuotaKB()), Long.valueOf(FolderConfig.getLimitULKB()));
		setCustomQuotaKB(q);
		return q;
	}

	/**
	 * Get the identifiers for the default quotas
	 * @return
	 */
	@Override
	public Set<String> getDefaultQuotaIdentifyers() {
		return defaultQuotas.keySet();
	}
	
	/**
	 * Get the default quota for the given identifyer or NULL if no such quota
	 * found
	 * 
	 * @param identifyer
	 * @return
	 */
	@Override
	public Quota getDefaultQuota(String identifyer) {
		return defaultQuotas.get(identifyer);
	}

	/**
	 * Get the quota (in KB) for this path. Important: Must provide a path with a
	 * valid base.
	 * 
	 * @param path
	 * @return Quota object.
	 */
	@Override
	public Quota getCustomQuota(String path) {
		StringBuilder query = new StringBuilder();
		query.append("select prop.name, prop.stringValue from ").append(Property.class.getName()).append(" as prop where ")
		     .append(" prop.category='").append(QUOTA_CATEGORY).append("'")
		     .append(" and prop.resourceTypeName='").append(quotaResource.getResourceableTypeName()).append("'")
		     .append(" and prop.resourceTypeId=").append(quotaResource.getResourceableId())
		     .append(" and prop.name=:name")
		     .append(" and prop.identity is null and prop.grp is null");
		
		List<Object[]> props = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Object[].class)
				.setParameter("name", path)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();
		if(props.isEmpty()) {
			return null;
		}
		Object[] p = props.get(0);
		return parseQuota((String)p[0], (String)p[1]);
	}

	/**
	 * Sets or updates the quota (in KB) for this path. Important: Must provide a
	 * path with a valid base.
	 * 
	 * @param quota
	 */
	@Override
	public void setCustomQuotaKB(Quota quota) {
		PropertyManager pm = PropertyManager.getInstance();
		Property p = pm.findProperty(null, null, quotaResource, QUOTA_CATEGORY, quota.getPath());
		if (p == null) { // create new entry
			p = pm.createPropertyInstance(null, null, quotaResource, QUOTA_CATEGORY, quota.getPath(), null, null, assembleQuota(quota), null);
			pm.saveProperty(p);
		} else {
			p.setStringValue(assembleQuota(quota));
			pm.updateProperty(p);
		}
		// if the quota is a default quota, rebuild the default quota list
		if (quota.getPath().startsWith(QuotaConstants.IDENTIFIER_DEFAULT)) {
			initDefaultQuotas();
		}
	}

	/**
	 * @param quota to be deleted
	 * @return true if quota successfully deleted or no such quota, false if quota
	 *         not deleted because it was a default quota that can not be deleted
	 */
	@Override
	public boolean deleteCustomQuota(Quota quota) {
		if (defaultQuotas == null) {
			throw new OLATRuntimeException(QuotaManagerImpl.class, "Quota manager has not been initialized properly! Must call init() first.", null);
		}
		// do not allow to delete default quotas!
		if (quota.getPath().startsWith(QuotaConstants.IDENTIFIER_DEFAULT)) {
			return false;
		}
		PropertyManager pm = PropertyManager.getInstance();
		Property p = pm.findProperty(null, null, quotaResource, QUOTA_CATEGORY, quota.getPath());
		if (p != null) pm.deleteProperty(p);
		return true;
	}

	/**
	 * Get a list of all objects which have an individual quota.
	 * 
	 * @return list of quotas.
	 */
	@Override
	public List<Quota> listCustomQuotasKB() {
		List<Quota> results = new ArrayList<>();
		PropertyManager pm = PropertyManager.getInstance();
		List<Property> props = pm.listProperties(null, null, quotaResource, QUOTA_CATEGORY, null);
		if (props == null || props.isEmpty()) return results;
		for (Iterator<Property> iter = props.iterator(); iter.hasNext();) {
			Property prop = iter.next();
			results.add(parseQuota(prop));
		}
		return results;
	}

	/**
	 * @param p
	 * @return Parsed quota object.
	 */
	private Quota parseQuota(Property p) {
		String s = p.getStringValue();
		return parseQuota(p.getName(), s);
	}
	
	/**
	 * 
	 * @param name Path of the quota
	 * @param s
	 * @return Parsed quota object.
	 */
	private Quota parseQuota(String name, String s) {
		int delim = s.indexOf(':');
		if (delim == -1) return null;
		Quota q = null;
		try {
			Long quotaKB = Long.valueOf(s.substring(0, delim));
			Long ulLimitKB = Long.valueOf(s.substring(delim + 1));
			q = createQuota(name, quotaKB, ulLimitKB);
		} catch (NumberFormatException e) {
			// will return null if quota parsing failed
		}
		return q;
	}

	private String assembleQuota(Quota quota) {
		return quota.getQuotaKB() + ":" + quota.getUlLimitKB();
	}

	/**
	 * call to get appropriate quota depending on role. Authors have normally
	 * bigger quotas than normal users.
	 * 
	 * @param identity
	 * @return
	 */
	@Override
	public Quota getDefaultQuotaDependingOnRole(Identity identity, Roles roles) {
		if (isPowerUser(roles)) {//TODO quota roles
			return getDefaultQuotaPowerUsers();
		}
		return getDefaultQuotaUsers();
	}

	/**
	 * call to get appropriate quota depending on role. Authors have normally
	 * bigger quotas than normal users. The method checks also if the user has a custom quota on the path specified. If yes the custom quota is retuned
	 * 
	 * @param identity
	 * @return custom quota or quota depending on role
	 */
	@Override
	public Quota getCustomQuotaOrDefaultDependingOnRole(Identity identity, Roles roles, String relPath) {
		Quota quota = getCustomQuota(relPath);
		if (quota == null) { // no custom quota
			//TODO quota roles
			Quota defQuota = isPowerUser(roles) ? getDefaultQuotaPowerUsers() : getDefaultQuotaUsers();
			return createQuota(relPath, defQuota.getQuotaKB(), defQuota.getUlLimitKB());
		}
		return quota;
	}
	
	private boolean isPowerUser(Roles roles) {
		return roles.isAdministrator() || roles.isLearnResourceManager() || roles.isAuthor();
	}

	/**
	 * get default quota for normal users. On places where you have users with
	 * different roles use
	 * 
	 * @see getDefaultQuotaDependingOnRole(Identity identity)
	 * @return Quota
	 */
	private Quota getDefaultQuotaUsers() {
		return defaultQuotas.get(QuotaConstants.IDENTIFIER_DEFAULT_USERS);
	}

	/**
	 * get default quota for power users (authors). On places where you have users
	 * with different roles use
	 * 
	 * @see getDefaultQuotaDependingOnRole(Identity identity)
	 * @return Quota
	 */
	private Quota getDefaultQuotaPowerUsers() {
		return defaultQuotas.get(QuotaConstants.IDENTIFIER_DEFAULT_POWER);
	}

	/**
	 * Return upload-limit depending on quota-limit and upload-limit values. 
	 * @param quotaKB2          Quota limit in KB, can be Quota.UNLIMITED
	 * @param uploadLimitKB2    Upload limit in KB, can be Quota.UNLIMITED
	 * @param currentContainer2 Upload container (folder)
	 * @return Upload limit on KB 
	 */
	@Override
	public int getUploadLimitKB(long quotaKB2, long uploadLimitKB2, VFSContainer currentContainer2) {
		if (quotaKB2 == Quota.UNLIMITED) {
			if (uploadLimitKB2 == Quota.UNLIMITED) {
				return Quota.UNLIMITED; // quote & upload un-limited
			} else {
				return (int)uploadLimitKB2;  // only upload limited
			}
		} else {
			// initialize default UL limit
			// prepare quota checks
			long quotaLeftKB = VFSManager.getQuotaLeftKB(currentContainer2);
			if (quotaLeftKB < 0) { 
				quotaLeftKB = 0; 
			}
			if (uploadLimitKB2 == Quota.UNLIMITED) {
				return (int)quotaLeftKB;// quote:limited / upload:unlimited 
			} else {
        // quote:limited / upload:limited 
				if (quotaLeftKB > uploadLimitKB2) {
					return (int)uploadLimitKB2; // upload limit cut the upload
				} else {
					return (int)quotaLeftKB; // quota-left space cut the upload
				}
			} 
		}	
	}
	
	/**
	 * Check if a quota path is valid
	 * @param path
	 * @return
	 */
	@Override
	public boolean isValidQuotaPath(String path) {
		if (path.startsWith(QuotaConstants.IDENTIFIER_DEFAULT) && !defaultQuotas.containsKey(path)) {
			return false;
		}
		return true;
	}

	@Override
	public Controller getQuotaEditorInstance(UserRequest ureq, WindowControl wControl, String relPath, List<? extends OrganisationRef> resourceOwnerships) {
		try {
			return new GenericQuotaEditController(ureq, wControl, relPath, resourceOwnerships);
		} catch (OLATSecurityException e) {
			log.warn("Try to access the quota editor without enough privilege", e);
			GenericQuotaViewController viewCtrl = new GenericQuotaViewController(ureq, wControl, relPath);
			viewCtrl.setNotEnoughPrivilegeMessage();
			return viewCtrl;
		}
	}
	
	@Override
	public Controller getQuotaViewInstance(UserRequest ureq, WindowControl wControl, String relPath) {
		return new GenericQuotaViewController(ureq, wControl, relPath);
	}

	@Override
	public boolean hasQuotaEditRights(Identity identity, Roles roles, List<OrganisationRef> organisationOwnerships) {
		return roles.hasRole(organisationOwnerships, OrganisationRoles.administrator)
				|| roles.hasRole(organisationOwnerships, OrganisationRoles.sysadmin)
				|| roles.hasRole(organisationOwnerships, OrganisationRoles.rolesmanager)
				|| roles.hasRole(organisationOwnerships, OrganisationRoles.usermanager)
				|| roles.hasRole(organisationOwnerships, OrganisationRoles.learnresourcemanager);
	}
}
