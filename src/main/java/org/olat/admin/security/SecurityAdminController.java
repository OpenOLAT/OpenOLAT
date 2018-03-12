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
package org.olat.admin.security;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderModule;
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
 * Initial date: 23.12.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SecurityAdminController extends FormBasicController {

	private static final String[] keys = new String[]{ "on" };
	private static final String[] values = new String[]{ "" };
	
	private MultipleSelectionElement wikiEl;
	private MultipleSelectionElement topFrameEl;
	private MultipleSelectionElement forceDownloadEl;
	
	private MultipleSelectionElement strictTransportSecurityEl;
	private MultipleSelectionElement xContentTypeOptionsEl;
	private MultipleSelectionElement xFrameOptionsSameoriginEl;

	private final FolderModule folderModule;
	private final BaseSecurityModule securityModule;
	
	public SecurityAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		securityModule = CoreSpringFactory.getImpl(BaseSecurityModule.class);
		folderModule = CoreSpringFactory.getImpl(FolderModule.class);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("sec.title");
		setFormDescription("sec.description");
		setFormContextHelp("Security");
		
		// on: force top top frame (more security); off: allow in frame (less security)
		topFrameEl = uifactory.addCheckboxesHorizontal("sec.topframe", "sec.topframe", formLayout, keys, values);
		topFrameEl.select("on", securityModule.isForceTopFrame());
		topFrameEl.addActionListener(FormEvent.ONCHANGE);
		topFrameEl.setEnabled(false);
		topFrameEl.setExampleKey("sec.top.frame.explanation", null);

		// on: send HTTP header X-FRAME-OPTIONS -> SAMEDOMAIN to prevent click-jack attacks. JS-top frame hack not save enough
		xFrameOptionsSameoriginEl = uifactory.addCheckboxesHorizontal("sec.xframe.sameorigin", "sec.xframe.sameorigin", formLayout, keys, values);
		xFrameOptionsSameoriginEl.addActionListener(FormEvent.ONCHANGE);
		if(securityModule.isXFrameOptionsSameoriginEnabled()) {
			xFrameOptionsSameoriginEl.select("on", true);
		}
		
		strictTransportSecurityEl = uifactory.addCheckboxesHorizontal("sec.strict.transport.sec", "sec.strict.transport.sec", formLayout, keys, values);
		strictTransportSecurityEl.addActionListener(FormEvent.ONCHANGE);
		if(securityModule.isStrictTransportSecurityEnabled()) {
			strictTransportSecurityEl.select("on", true);
		}
		
		xContentTypeOptionsEl = uifactory.addCheckboxesHorizontal("sec.content.type.options", "sec.content.type.options", formLayout, keys, values);
		xContentTypeOptionsEl.addActionListener(FormEvent.ONCHANGE);
		if(securityModule.isXContentTypeOptionsEnabled()) {
			xContentTypeOptionsEl.select("on", true);
		}

		// on: block wiki (more security); off: do not block wiki (less security)
		wikiEl = uifactory.addCheckboxesHorizontal("sec.wiki", "sec.wiki", formLayout, keys, values);
		wikiEl.addActionListener(FormEvent.ONCHANGE);
		if(!securityModule.isWikiEnabled()) {
			wikiEl.select("on", true);
		}

		// on: force file download in folder component (more security); off: allow execution of content (less security)
		forceDownloadEl = uifactory.addCheckboxesHorizontal("sec.download", "sec.force.download", formLayout, keys, values);
		forceDownloadEl.addActionListener(FormEvent.ONCHANGE);
		if(folderModule.isForceDownload()) {
			forceDownloadEl.select("on", true);
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(topFrameEl == source) {
			boolean enabled = topFrameEl.isAtLeastSelected(1);
			securityModule.setForceTopFrame(enabled);
		} else if(wikiEl == source) {
			securityModule.setWikiEnabled(!wikiEl.isAtLeastSelected(1));
			// update collaboration tools list
			CollaborationToolsFactory.getInstance().initAvailableTools();
		} else if(xFrameOptionsSameoriginEl == source) {
			securityModule.setXFrameOptionsSameoriginEnabled(xFrameOptionsSameoriginEl.isAtLeastSelected(1));
		} else if(strictTransportSecurityEl == source) {
			securityModule.setStrictTransportSecurity(strictTransportSecurityEl.isAtLeastSelected(1));
		} else if(xContentTypeOptionsEl == source) {
			securityModule.setxContentTypeOptions(xContentTypeOptionsEl.isAtLeastSelected(1));
		} else if(forceDownloadEl == source) {
			folderModule.setForceDownload(forceDownloadEl.isAtLeastSelected(1));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}