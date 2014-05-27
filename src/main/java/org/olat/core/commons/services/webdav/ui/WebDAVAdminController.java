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
package org.olat.core.commons.services.webdav.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.webdav.WebDAVModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WebDAVAdminController extends FormBasicController {
	
	private MultipleSelectionElement enableModuleEl, enableLinkEl, enableDigestEl;
	private final WebDAVModule webDAVModule;
	
	public WebDAVAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		webDAVModule = CoreSpringFactory.getImpl(WebDAVModule.class);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		setFormTitle("admin.menu.title.alt");
		setFormDescription("admin.webdav.description");
		setFormContextHelp(WebDAVAdminController.class.getPackage().getName(), "webdavconfig.html", "help.hover.webdavconfig");


		boolean enabled = webDAVModule.isEnabled();
		String[] values = new String[] { getTranslator().translate("webdav.on") };
		enableModuleEl = uifactory.addCheckboxesHorizontal("webdavModule", "webdav.module", formLayout, new String[]{"xx"}, values);
		enableModuleEl.select("xx", enabled);
		enableModuleEl.addActionListener(FormEvent.ONCHANGE);
		
		enableLinkEl = uifactory.addCheckboxesHorizontal("webdavLink", "webdav.link", formLayout, new String[]{"xx"}, values);
		enableLinkEl.select("xx", webDAVModule.isLinkEnabled());
		enableLinkEl.addActionListener(FormEvent.ONCHANGE);
		enableLinkEl.setEnabled(enabled);
		
		enableDigestEl = uifactory.addCheckboxesHorizontal("webdavDigest", "webdav.digest", formLayout, new String[]{"xx"}, values);
		enableDigestEl.select("xx", webDAVModule.isDigestAuthenticationEnabled());
		enableDigestEl.addActionListener(FormEvent.ONCHANGE);
		enableDigestEl.setEnabled(enabled);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == enableModuleEl) {
			boolean enabled = enableModuleEl.isAtLeastSelected(1);
			webDAVModule.setEnabled(enabled);
			enableLinkEl.setEnabled(enabled);
			enableDigestEl.setEnabled(enabled);
		} else if(source == enableLinkEl) {
			boolean enabled = enableLinkEl.isAtLeastSelected(1);
			webDAVModule.setLinkEnabled(enabled);
		} else if(source == enableDigestEl) {
			boolean enabled = enableDigestEl.isAtLeastSelected(1);
			webDAVModule.setDigestAuthenticationEnabled(enabled);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
