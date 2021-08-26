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
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.logging.AssertException;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<BR>
 * Generic editor controller for quotas. Can be constructed from a quota or a
 * folder path. When finished the controller fires the following events:<BR>
 * Event.CHANGED_EVENT
 * <p>
 * Check with QuotaManager.hasQuotaEditRights if you are allowed to use this
 * controller. Fires an exception if user is not allowed to call controller.
 * <P>
 * Initial Date:  Dec 22, 2004
 *
 * @author gnaegi
 */
class GenericQuotaEditController extends BasicController {

	private QuotaForm quotaForm;
	private Link addQuotaButton;
	private VelocityContainer myContent;

	private Quota currentQuota;
	private final boolean canEditQuota;
	private final boolean withCancel;
	
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
	GenericQuotaEditController(UserRequest ureq, WindowControl wControl, String relPath,
			boolean embedded, boolean withCancel) {
		super(ureq, wControl);
		this.withCancel = withCancel;

		currentQuota = quotaManager.getCustomQuota(relPath);
		if (currentQuota == null) {
			currentQuota = quotaManager.createQuota(relPath, null, null);		
		}
		canEditQuota = quotaManager.hasQuotaEditRights(ureq.getIdentity(), ureq.getUserSession().getRoles(), currentQuota);

		initMyContent();
		initQuotaForm(ureq, currentQuota, canEditQuota);
		myContent.contextPut("withLegend", Boolean.valueOf(embedded));
		putInitialPanel(myContent);
	}

	/**
	 * Constructor for the generic quota edit controller used when an existing quota should be
	 * edited, as done in the admin quotamanagement
	 * @param ureq
	 * @param wControl
	 * @param quota The existing quota or null. If null, a new quota is generated
	 */
	public GenericQuotaEditController(UserRequest ureq, WindowControl wControl, Quota quota, boolean withLegend) {
		super(ureq, wControl);
		withCancel = true;
		currentQuota = quota;
		canEditQuota = quotaManager.hasQuotaEditRights(ureq.getIdentity(), ureq.getUserSession().getRoles(), currentQuota);
		
		initMyContent();
		initQuotaForm(ureq, currentQuota, canEditQuota);
		myContent.contextPut("withLegend", Boolean.valueOf(withLegend));
		putInitialPanel(myContent);
	}

	/**
	 * Constructor for the generic quota edit controller used when a new
	 * existing quota should be edited.
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public GenericQuotaEditController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		withCancel = true;
		canEditQuota = true;	
		currentQuota = quotaManager.createQuota(null, null, null);

		// start with new quota
		initMyContent();
		initQuotaForm(ureq, currentQuota, false);
		myContent.contextPut("isEmptyQuota", Boolean.TRUE);
		myContent.contextPut("withLegend", Boolean.FALSE);
		putInitialPanel(myContent);
	}

	private void initMyContent() {
		myContent = createVelocityContainer("edit");
		
		if(canEditQuota) {
			addQuotaButton = LinkFactory.createButtonSmall("qf.new", myContent, this);
		}
		
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
	
	private void initQuotaForm(UserRequest ureq, Quota quota, boolean withDelete) {
		if (quotaForm != null) {
			removeAsListenerAndDispose(quotaForm);
		}
		quotaForm = new QuotaForm(ureq, getWindowControl(), quota, canEditQuota, withDelete, withCancel);
		listenTo(quotaForm);
		myContent.put("quotaform", quotaForm.getInitialComponent());
		myContent.contextPut("editQuota", Boolean.TRUE);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == addQuotaButton) {
			doAddQuota(ureq);
		}
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == quotaForm) {
			if (event == Event.DONE_EVENT) {
				doSaveQuota(ureq);
			} else if(event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, event);
			} else if(event instanceof DeleteQuotaEvent) {
				doDelete(ureq);
			}
		}
	}

	public Quota getQuota() {
		if (currentQuota == null) {
			throw new AssertException("getQuota called but currentQuota is null");
		}
		return currentQuota;
	}
	
	private void doAddQuota(UserRequest ureq) {
		initQuotaForm(ureq, currentQuota, canEditQuota);
	}
	
	private void doSaveQuota(UserRequest ureq) {
		currentQuota = quotaManager.createQuota(quotaForm.getPath(),
				Long.valueOf(quotaForm.getQuotaKB()), Long.valueOf(quotaForm.getULLimit()));
		quotaManager.setCustomQuotaKB(currentQuota);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doDelete(UserRequest ureq) {
		boolean deleted = quotaManager.deleteCustomQuota(currentQuota);
		if (deleted) {
			myContent.remove(quotaForm.getInitialComponent());
			myContent.contextPut("editQuota", Boolean.FALSE);
			showInfo("qf.deleted", currentQuota.getPath());
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else {
			showError("qf.cannot.del.default");
		}
	}
		
	@Override
	protected void doDispose() {
		//
	}
}
