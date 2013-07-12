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

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;

/**
 * Initial Date:  08.07.2003
 *
 * @author Mike Stock
 */
public class QuotaForm extends FormBasicController {

	private TextElement path;
	private IntegerElement quotaKB;
	private IntegerElement ulLimitKB;
	
	private Quota quota;
	private final boolean editable;
	
	/**
	 * @param name component name of form
	 * @param quota the quota used to initialize the form or null if empty form is used
	 */
	public QuotaForm(UserRequest ureq, WindowControl wControl, Quota quota, boolean editable) {
		super(ureq, wControl);
		this.quota = quota;
		this.editable = editable;
		initForm(ureq);
	}

	/**
	 * @return The path.
	 */
	public String getPath() {
		return path.getValue();
	}

	/**
	 * @return Quota in KB.
	 */
	public String getQuotaKB() {
		return quotaKB.getValue();
	}

	/**
	 * @return Upload limit in KB
	 */
	public String getULLimit() {
		return ulLimitKB.getValue();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected boolean validateFormLogic (UserRequest ureq) {
		if (!QuotaManager.getInstance().isValidQuotaPath(path.getValue())) {
			path.setErrorKey("qf.error.path.invalid", null);
			return false;	
		}
		return true;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		if (quota != null && quota.getPath() != null && !quota.getPath().equals("")) {
			path = uifactory.addTextElement("qf_path", "qf.path", 255, quota.getPath(), formLayout);
			if (quota != null) path.setEnabled(false);
		} else {
			path = uifactory.addTextElement("qf_path", "qf.path", 255, "", formLayout);
			path.setNotEmptyCheck("qf.error.path.invalid");
			path.setMandatory(true);
		}
		path.setEnabled(editable);
		
		if (quota != null && quota.getQuotaKB() != null) {
			quotaKB = uifactory.addIntegerElement("qf_quota", "qf.quota", quota.getQuotaKB().intValue(), formLayout);
		} else {	
			quotaKB = uifactory.addIntegerElement("qf_quota", "qf.quota",(int)FolderConfig.getDefaultQuotaKB() , formLayout);
		}
		quotaKB.setMandatory(true);
		quotaKB.setEnabled(editable);
		
		if (quota != null && quota.getUlLimitKB() != null) {
			ulLimitKB = uifactory.addIntegerElement("qf_limit", "qf.limit", quota.getUlLimitKB().intValue(), formLayout);
		} else {
			ulLimitKB = uifactory.addIntegerElement("qf_limit", "qf.limit",(int)FolderConfig.getLimitULKB() , formLayout);
		}
		ulLimitKB.setMandatory(true);
		ulLimitKB.setEnabled(editable);
		
		if(editable) {
			uifactory.addFormSubmitButton("submit", formLayout);
		}
	}

	@Override
	protected void doDispose() {
		//
	}
}
