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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  08.07.2003
 *
 * @author Mike Stock
 */
public class QuotaForm extends FormBasicController {
	
	private static final long MAX_QUOTA = 5l * 1000l * 1000l * 1000l;

	private TextElement path;
	private TextElement quotaKB;
	private TextElement ulLimitKB;
	private FormLink deleteButton;
	
	private Quota quota;
	private final boolean editable;
	private final boolean deletable;
	private final boolean cancelable;
	
	@Autowired
	private QuotaManager quotaManager;
	
	/**
	 * @param name component name of form
	 * @param quota the quota used to initialize the form or null if empty form is used
	 */
	public QuotaForm(UserRequest ureq, WindowControl wControl, Quota quota,
			boolean editable, boolean deletable, boolean cancelable) {
		super(ureq, wControl);
		this.quota = quota;
		this.editable = editable;
		this.deletable = deletable;
		this.cancelable = cancelable;
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
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (quota != null && quota.getPath() != null && !quota.getPath().equals("")) {
			path = uifactory.addTextElement("qf_path", "qf.path", 255, quota.getPath(), formLayout);
			path.setEnabled(false);
		} else {
			path = uifactory.addTextElement("qf_path", "qf.path", 255, "", formLayout);
			path.setNotEmptyCheck("qf.error.path.invalid");
			path.setMandatory(true);
		}
		path.setEnabled(editable);
		
		if (quota != null && quota.getQuotaKB() != null) {
			quotaKB = uifactory.addTextElement("qf_quota", "qf.quota", 16, String.valueOf(quota.getQuotaKB()), formLayout);
		} else {	
			quotaKB = uifactory.addTextElement("qf_quota", "qf.quota", 16, String.valueOf(FolderConfig.getDefaultQuotaKB()), formLayout);
		}
		quotaKB.setMandatory(true);
		quotaKB.setEnabled(editable);
		
		if (quota != null && quota.getUlLimitKB() != null) {
			ulLimitKB = uifactory.addTextElement("qf_limit", "qf.limit", 16, String.valueOf(quota.getUlLimitKB()), formLayout);
		} else {
			ulLimitKB = uifactory.addTextElement("qf_limit", "qf.limit", 16, String.valueOf(FolderConfig.getLimitULKB()), formLayout);
		}
		ulLimitKB.setMandatory(true);
		ulLimitKB.setEnabled(editable);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonLayout);
		
		if(cancelable) {
			uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		}
		if(editable) {
			uifactory.addFormSubmitButton("submit", buttonLayout);
		}
		if(editable && deletable) {
			deleteButton = uifactory.addFormLink("qf.del", buttonLayout, Link.BUTTON);
		}
	}

	@Override
	protected boolean validateFormLogic (UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (!quotaManager.isValidQuotaPath(path.getValue())) {
			path.setErrorKey("qf.error.path.invalid", null);
			allOk &= false;	
		}
		allOk &= validateQuota(quotaKB);
		allOk &= validateQuota(ulLimitKB);
		return allOk;
	}
	
	private boolean validateQuota(TextElement textEl) {
		boolean allOk = true;
		
		textEl.clearError();
		if(textEl.isEnabled()) {
			
			if(!StringHelper.containsNonWhitespace(textEl.getValue())) {
				textEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			} else if(!StringHelper.isLong(textEl.getValue())) {
				textEl.setErrorKey("form.error.nointeger", null);
				allOk &= false;
			} else {
				try {
					long val = Long.parseLong(textEl.getValue());
					if(val <= 0 || val > MAX_QUOTA) {
						textEl.setErrorKey("error.quota.range", new String[] { Long.toString(MAX_QUOTA), Formatter.formatBytes(MAX_QUOTA * 1000l) });
						allOk &= false;
					}
				} catch (NumberFormatException e) {
					textEl.setErrorKey("form.error.nointeger", null);
					allOk &= false;
				}
			}
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == deleteButton) {
			fireEvent(ureq, new DeleteQuotaEvent());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
