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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.07.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GenericQuotaViewController extends BasicController {

	private VelocityContainer myContent;
	private QuotaForm quotaForm;
	private Quota currentQuota;
	
	@Autowired
	private QuotaManager quotaManager;


	/**
	 * Constructor for the generic quota edit controller used to change a quota anywhere in the 
	 * system not using the generic quota management. Instead of using a quota the 
	 * constructor takes the folder path for which the quota will be changed.
	 * <p>
	 * To create an instance of this controller, use QuotaManager's factory method 
	 * @param ureq
	 * @param wControl
	 * @param quotaPath The path for which the quota should be edited
	 */
	GenericQuotaViewController(UserRequest ureq, WindowControl wControl, String relPath) {
		super(ureq, wControl);
		
		// check if quota foqf.cannot.del.defaultr this path already exists
		currentQuota = quotaManager.getCustomQuota(relPath);
		// init velocity context
		initMyContent();
		if (currentQuota == null) {
			currentQuota = quotaManager.createQuota(relPath, null, null);		
		} else {
			initQuotaForm(ureq, currentQuota);			
		}
		myContent.contextPut("editQuota", Boolean.FALSE);	
		putInitialPanel(myContent);
	}
	
	public void setNotEnoughPrivilegeMessage() {
		myContent.contextPut("notEnoughPrivilege", Boolean.TRUE);
	}

	private void initMyContent() {
		myContent = createVelocityContainer("edit");
		myContent.contextPut("notEnoughPrivilege", Boolean.FALSE);
		
		String defQuotaIdent = quotaManager.getDefaultQuotaIdentifier(currentQuota);
		initDefaultQuota("users", QuotaConstants.IDENTIFIER_DEFAULT_USERS, defQuotaIdent);
		initDefaultQuota("powerusers", QuotaConstants.IDENTIFIER_DEFAULT_POWER, defQuotaIdent);
		initDefaultQuota("groups", QuotaConstants.IDENTIFIER_DEFAULT_GROUPS, defQuotaIdent);
		initDefaultQuota("repository", QuotaConstants.IDENTIFIER_DEFAULT_REPO, defQuotaIdent);
		initDefaultQuota("coursefolder", QuotaConstants.IDENTIFIER_DEFAULT_COURSE, defQuotaIdent);
		initDefaultQuota("coursedocuments", QuotaConstants.IDENTIFIER_DEFAULT_DOCUMENTS, defQuotaIdent);
		initDefaultQuota("coachfolder", QuotaConstants.IDENTIFIER_DEFAULT_COACHFOLDER, defQuotaIdent);
		initDefaultQuota("nodefolder", QuotaConstants.IDENTIFIER_DEFAULT_NODES, defQuotaIdent);
		initDefaultQuota("pfNodefolder", QuotaConstants.IDENTIFIER_DEFAULT_PFNODES, defQuotaIdent);
		initDefaultQuota("feeds", QuotaConstants.IDENTIFIER_DEFAULT_FEEDS, defQuotaIdent);
	}
	
	private void initDefaultQuota(String key, String identifier, String defaultIdentifier) {
		myContent.contextPut(key, quotaManager.getDefaultQuota(identifier));
		myContent.contextPut(key.concat("Def"), identifier.equals(defaultIdentifier));
	}
	
	private void initQuotaForm(UserRequest ureq, Quota quota) {
		if (quotaForm != null) {
			removeAsListenerAndDispose(quotaForm);
		}
		quotaForm = new QuotaForm(ureq, getWindowControl(), quota, false, false, false);
		listenTo(quotaForm);
		myContent.put("quotaform", quotaForm.getInitialComponent());
		myContent.contextPut("editQuota", Boolean.TRUE);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
