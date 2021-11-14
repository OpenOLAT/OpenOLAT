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

package org.olat.group.ui.wizard;

import org.olat.admin.quota.QuotaForm;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.Quota;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BGConfigQuotaController extends FormBasicController {

	private IntegerElement quotaKB;
	private IntegerElement ulLimitKB;
	
	private Quota quota;
	
	/**
	 * @param name component name of form
	 * @param quota the quota used to initialize the form or null if empty form is used
	 */
	public BGConfigQuotaController(UserRequest ureq, WindowControl wControl, Quota quota) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(QuotaForm.class, getLocale(), getTranslator()));
		this.quota = quota;
		initForm(ureq);
	}
	
	public BGConfigQuotaController(UserRequest ureq, WindowControl wControl, Quota quota, Form rootForm) {
		super(ureq, wControl, -1, null, rootForm);
		setTranslator(Util.createPackageTranslator(QuotaForm.class, getLocale(), getTranslator()));
		this.quota = quota;
		initForm(ureq);
	}

	/**
	 * @return Quota in KB.
	 */
	public Long getQuotaKB() {
		if(StringHelper.isLong(quotaKB.getValue())) {
			return Long.valueOf(quotaKB.getValue());
		}
		return null;
	}

	/**
	 * @return Upload limit in KB
	 */
	public Long getULLimit() {
		if(StringHelper.isLong(ulLimitKB.getValue())) {
			return Long.valueOf(ulLimitKB.getValue());
		}
		return null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("qf.edit");
		
		if (quota != null && quota.getQuotaKB() != null) {
			quotaKB = uifactory.addIntegerElement("qf_quota", "qf.quota", quota.getQuotaKB().intValue(), formLayout);
		} else {	
			quotaKB = uifactory.addIntegerElement("qf_quota", "qf.quota",(int)FolderConfig.getDefaultQuotaKB() , formLayout);
		}
		quotaKB.setMandatory(true);
		
		if (quota != null && quota.getUlLimitKB() != null) {
			ulLimitKB = uifactory.addIntegerElement("qf_limit", "qf.limit", quota.getUlLimitKB().intValue(), formLayout);
		} else {
			ulLimitKB = uifactory.addIntegerElement("qf_limit", "qf.limit",(int)FolderConfig.getLimitULKB() , formLayout);
		}
		ulLimitKB.setMandatory(true);
	}
}
