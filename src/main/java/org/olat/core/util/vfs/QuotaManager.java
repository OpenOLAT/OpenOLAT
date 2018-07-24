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
* <p>
*/ 

package org.olat.core.util.vfs;

import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;

/**
 * Initial Date: Feb 17, 2004
 * @author Mike Stock 
 * <br>
 * Comment: Original author
 * <p>
 * @author Florian Gnägi
 * <br>
 * Comment: Refactoring to core package make default quotas generic
 */
public interface QuotaManager {

	/**
	 * Create a quota object (transient, not yet stored)
	 * @param path
	 * @param quotaKB
	 * @param ulLimitKB
	 * @return
	 */
	public Quota createQuota(String path, Long quotaKB, Long ulLimitKB);
	
	/**
	 * Get the identifyers for the default quotas
	 * @return
	 */
	public Set<String> getDefaultQuotaIdentifyers();
	
	/**
	 * Get the default quota for the given identifyer or NULL if no such quota
	 * found
	 * 
	 * @param identifyer
	 * @return
	 */
	public Quota getDefaultQuota(String identifyer);

	/**
	 * Get the quota (in KB) for this path. Important: Must provide a path with a
	 * valid base.
	 * 
	 * @param path
	 * @return Quota object.
	 */
	public Quota getCustomQuota(String path);

	/**
	 * Sets or updates the quota (in KB) for this path. Important: Must provide a
	 * path with a valid base.
	 * 
	 * @param quota
	 */
	public void setCustomQuotaKB(Quota quota);
	
	/**
	 * @param quota to be deleted
	 * @return true if quota successfully deleted or no such quota, false if quota
	 *         not deleted because it was a default quota that can not be deleted
	 */
	public boolean deleteCustomQuota(Quota quota);

	/**
	 * Get a list of all objects which have an individual quota.
	 * 
	 * @return list of quotas.
	 */
	public List<Quota> listCustomQuotasKB();

	/**
	 * call to get appropriate quota depending on role. Authors have normally
	 * bigger quotas than normal users.
	 * 
	 * @param identity
	 * @return
	 */
	public Quota getDefaultQuotaDependingOnRole(Identity identity, Roles role);

	/**
	 * call to get appropriate quota depending on role. Authors have normally
	 * bigger quotas than normal users. The method checks also if the user has a custom quota on the path specified. If yes the custom quota is retuned
	 * 
	 * @param identity
	 * @return custom quota or quota depending on role
	 */
	public Quota getCustomQuotaOrDefaultDependingOnRole(Identity identity, Roles roles, String relPath);

	/**
	 * Return upload-limit depending on quota-limit and upload-limit values. 
	 * @param quotaKB2          Quota limit in KB, can be Quota.UNLIMITED
	 * @param uploadLimitKB2    Upload limit in KB, can be Quota.UNLIMITED
	 * @param currentContainer2 Upload container (folder)
	 * @return Upload limit on KB 
	 */
	public int getUploadLimitKB(long quotaKB2, long uploadLimitKB2, VFSContainer currentContainer2);
	
	/**
	 * Check if a quota path is valid
	 * @param path
	 * @return
	 */
	public boolean isValidQuotaPath(String path);
	
	/**
	 * Factory method to create a controller that is capable of editing the
	 * quota for the given path. If the user has not enough privileges to edit
	 * the quota, it will only see the quota's view.
	 * <p>
	 * The controller must fire the following events:
	 * <ul> 
	 * <li>Event.CANCELLED_EVENT</li>
	 * </ul>
	 * @param ureq
	 * @param wControl
	 * @param relPath Path of the quota
	 * @param withLegend Add the title as legend of the form
	 * @param withCancel Add a cancel button
	 * @return
	 */
	public Controller getQuotaEditorInstance(UserRequest ureq, WindowControl wControl, String relPath,
			boolean withLegend, boolean withCancel);
	
	public Controller getQuotaViewInstance(UserRequest ureq, WindowControl wControl, String relPath);
	
	/**
	 * Check quickly if the user has the minimal roles to edit some quota.
	 * To check permission on a specific quota, use the method below.
	 * 
	 * @param roles
	 * @return
	 */
	public boolean hasMinimalRolesToEditquota(Roles roles);
	
	/**
	 * Check if a user has the rights to launch the quota editor tool
	 * 
	 * @param identity The identity that requests to change a quota
	 * @return true: user is allowed to launch quota editor ; false: user is not
	 *         allowed to launch quota editor
	 */
	public boolean hasQuotaEditRights(Identity identity, Roles roles, Quota quota);
	
	/**
	 * Check if a user has the rights to launch the quota editor tool
	 * 
	 * @param identity The identity that requests to change a quota
	 * @return true: user is allowed to launch quota editor ; false: user is not
	 *         allowed to launch quota editor
	 */
	public boolean hasQuotaEditRights(Identity identity, Roles roles, List<OrganisationRef> owners);
}
